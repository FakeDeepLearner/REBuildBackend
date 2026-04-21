package com.rebuild.backend.service.util_services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.user_services.ProfileService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class CloudinaryService {

    private static final Executor taskExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private final Cloudinary cloudinary;

    private final ProfileRepository profileRepository;

    private final ProfileService profileService;

    @Autowired
    public CloudinaryService(Cloudinary cloudinary,
                             ProfileRepository profileRepository, ProfileService profileService) {
        this.cloudinary = cloudinary;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }


    public String generateTimedUrlForPictureId(String pictureId){
        try {
            long expiryTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond();
            return cloudinary.privateDownload(pictureId, "jpg",
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
        UserProfile profile = removingUser.getUserProfile();

        String profilePictureId = profile.getPictureId();
        if (profilePictureId != null) {
            scheduleDeletion(profilePictureId);
            profile.setPictureId(null);
        }
        if (saveAndReturn) {
            return profileRepository.save(profile);
        } else {
            return profile;
        }
    }

    public UserProfileResponse removeProfilePicture(User removingUser)
    {
        UserProfile profile = removingUser.getUserProfile();

        String profilePictureId = profile.getPictureId();

        if (profilePictureId != null) {
            scheduleDeletion(profilePictureId);
            profile.setPictureId(null);
        }

        profileRepository.save(profile);
        return profileService.loadSelfProfile(removingUser);
    }

    private String createNewPicture(MultipartFile pictureFile) throws IOException {

        Transformation transformation = new Transformation<>().
                flags("force_strip").fetchFormat("jpg");
        Map uploadResult = cloudinary.uploader().upload(pictureFile.getInputStream(),
                ObjectUtils.asMap("type", "private",
                        "transformation", transformation));

        return (String) uploadResult.get("public_id");
    }


    private UserProfile modifyPicture(User changingUser, MultipartFile pictureFile)
    {
        UserProfile newProfile = null;
        try {
            String detectedFileType = new Tika().detect(pictureFile.getInputStream());
            if (!"image/jpeg".equals(detectedFileType) && !"image.png".equals(detectedFileType))
            {
                throw new FileUploadException(HttpStatus.BAD_REQUEST, "The file is not of jpeg or png");
            }

            if (!pictureFile.isEmpty())
            {
                newProfile = removeProfilePicture(changingUser, false);

                String newPictureId = createNewPicture(pictureFile);
                newProfile.setPictureId(newPictureId);

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
                new ProfileSensitiveInformationDTO(generateTimedUrlForPictureId(changedProfile.getPictureId()),
                        changingUser.getEmail(), changingUser.getPhoneNumber()),

                changingUser.getForumUsername(), changedProfile.getMadeComments(), changedProfile.getMadePosts()
        );
    }
}
