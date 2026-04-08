package com.rebuild.backend.service.util_services;

import com.cloudinary.Cloudinary;
import com.cloudinary.EagerTransformation;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.user_services.ProfileService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class CloudinaryService {

    private static final Executor taskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final Cloudinary cloudinary;

    private final ProfilePictureRepository profilePictureRepository;

    private final ProfileRepository profileRepository;

    private final ProfileService profileService;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary, ProfilePictureRepository profilePictureRepository,
                             ProfileRepository profileRepository, ProfileService profileService) {
        this.cloudinary = cloudinary;
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }


    public String generateTimedUrlForPicture(ProfilePicture profilePicture){
        try {
            long expiryTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond();
            return cloudinary.privateDownload(profilePicture.getPublic_id(), "jpg",
                    ObjectUtils.asMap("expires_at", expiryTimestamp));
        }
        catch (Exception _) {
           return null;
        }
    }

    private CompletableFuture<Void> scheduleDeletion(String public_id)
    {
        return CompletableFuture.runAsync(() ->
        {
            try {
                cloudinary.uploader().destroy(public_id,
                        ObjectUtils.asMap("type", "private"));
            }
            catch (IOException _)
            {

            }
        }, taskExecutor);

    }


    public UserProfile removeProfilePicture(User removingUser, boolean saveAndReturn){
        UserProfile profile = profileRepository.findByUser(removingUser).orElseThrow(
                () -> new NotFoundException("The specified user is not found")
        );

        if (profile.getProfilePicture() != null) {
            scheduleDeletion(profile.getProfilePicture().getPublic_id());
            profilePictureRepository.deleteById(profile.getProfilePicture().getId());
            profile.setProfilePicture(null);
        }
        if (saveAndReturn) {
            return profileRepository.save(profile);
        } else {
            return profile;
        }
    }

    public UserProfileResponse removeProfilePicture(User removingUser)
    {
        UserProfile profile = profileRepository.findByUser(removingUser).orElseThrow(
                () -> new NotFoundException("The specified user is not found")
        );

        if (profile.getProfilePicture() != null) {
            scheduleDeletion(profile.getProfilePicture().getPublic_id());
            profilePictureRepository.deleteById(profile.getProfilePicture().getId());
            profile.setProfilePicture(null);
        }
        return profileService.loadUserProfile(removingUser, removingUser.getId());
    }

    private ProfilePicture createNewPicture(MultipartFile pictureFile) throws IOException {

        Transformation transformation = new Transformation<>().
                flags("force_strip").fetchFormat("jpg");
        Map uploadResult = cloudinary.uploader().upload(pictureFile.getInputStream(),
                ObjectUtils.asMap("type", "private",
                        "transformation", transformation));

        return new ProfilePicture((String) uploadResult.get("public_id"),
                (String) uploadResult.get("asset_id"), Instant.now());
    }


    private UserProfile modifyPicture(User changingUser, MultipartFile pictureFile)
    {
        UserProfile newProfile = null;
        try {
            String detectedFileType = new Tika().detect(pictureFile.getInputStream());
            if (!"image/jpeg".equals(detectedFileType) && !"image.png".equals(detectedFileType))
            {
                throw new FileUploadException(HttpStatus.BAD_REQUEST, "The file is not of jpeg or png type");
            }

            if (!pictureFile.isEmpty())
            {
                newProfile = removeProfilePicture(changingUser, false);

                ProfilePicture profilePicture = createNewPicture(pictureFile);
                newProfile.setProfilePicture(profilePicture);

            }

        }
        catch (IOException _)
        {
            throw new FileUploadException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }

        if (newProfile == null)
        {
            return changingUser.getUserProfile();
        }

        return profileRepository.save(newProfile);
    }

    public UserProfile modifyProfilePictureOf(User changingUser, MultipartFile pictureFile)
    {

        return modifyPicture(changingUser, pictureFile);
    }


    public UserProfileResponse changeProfilePicture(User changingUser, MultipartFile pictureFile)
    {

        UserProfile changedProfile = modifyPicture(changingUser, pictureFile);


        return new UserProfileResponse(
                new ProfileSensitiveInformationDTO(generateTimedUrlForPicture(changedProfile.getProfilePicture()),
                        changingUser.getEmail(), changingUser.getPhoneNumber()),

                changingUser.getForumUsername(), changedProfile.getMadeComments(), changedProfile.getMadePosts()
        );
    }
}
