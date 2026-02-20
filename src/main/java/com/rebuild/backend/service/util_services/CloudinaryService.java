package com.rebuild.backend.service.util_services;

import com.cloudinary.Cloudinary;
import com.cloudinary.EagerTransformation;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.FileUploadException;
import com.rebuild.backend.repository.user_repositories.ProfilePictureRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CloudinaryService(Cloudinary cloudinary, ProfilePictureRepository profilePictureRepository,
                             ProfileRepository profileRepository) {
        this.cloudinary = cloudinary;
        this.profilePictureRepository = profilePictureRepository;
        this.profileRepository = profileRepository;
    }


    public String generateTimedUrlForPicture(ProfilePicture profilePicture){
        try {
            long expiryTimestamp = Instant.now().plus(5, ChronoUnit.MINUTES).getEpochSecond();
            return cloudinary.privateDownload(profilePicture.getPublic_id(), "jpg",
                    ObjectUtils.asMap("expires_at", expiryTimestamp));
        }
        catch (Exception e) {
            throw new FileUploadException(e.getMessage(), e.getCause());
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
        UserProfile profile = profileRepository.findByUser(removingUser);

        if (profile.getProfilePicture() != null) {
            scheduleDeletion(profile.getProfilePicture().getPublic_id());
            profilePictureRepository.deleteProfilePictureByPublic_id(profile.getProfilePicture().getPublic_id());
            profile.setProfilePicture(null);
        }
        if (saveAndReturn) {
            return profileRepository.save(profile);
        } else {
            return profile;
        }
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


    public UserProfile modifyProfilePictureOf(User changingUser, MultipartFile pictureFile) throws IOException
    {
        List<String> allowedTypes = List.of("image/jpeg", "image/png");

        String fileContent = pictureFile.getContentType();

        if (fileContent == null || !allowedTypes.contains(fileContent)){
            return null;
        }

        if (!pictureFile.isEmpty())
        {
            UserProfile pictureRemovedProfile = removeProfilePicture(changingUser, false);

            ProfilePicture profilePicture = createNewPicture(pictureFile);
            pictureRemovedProfile.setProfilePicture(profilePicture);
            profilePicture.setAssociatedProfile(pictureRemovedProfile);
            return profileRepository.save(pictureRemovedProfile);
        }
        return changingUser.getUserProfile();
    }
}
