package com.rebuild.backend.service.util_services;

import com.cloudinary.Cloudinary;
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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class CloudinaryService {

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
            return cloudinary.privateDownload(profilePicture.getPublic_id(), "png",
                    ObjectUtils.asMap("expires_at", expiryTimestamp));
        }
        catch (Exception e) {
            throw new FileUploadException(e.getMessage(), e.getCause());
        }
    }

    public UserProfile modifyProfilePictureOf(User chngingUser, MultipartFile pictureFile) throws IOException
    {
        UserProfile profile = profileRepository.findByUser(chngingUser);
        if (!pictureFile.isEmpty())
        {
            if(profile.getProfilePicture() != null)
            {
                profilePictureRepository.deleteProfilePictureByPublic_id(profile.getProfilePicture().getPublic_id());
                cloudinary.uploader().destroy(profile.getProfilePicture().getPublic_id(),
                        ObjectUtils.asMap("type", "private"));
            }


            @SuppressWarnings("JvmTaintAnalysis")
            Map uploadResult = cloudinary.uploader().upload(FileCopyUtils.
                            copyToByteArray(pictureFile.getInputStream()),
                    ObjectUtils.asMap("type", "private"));
            ProfilePicture profilePicture = new ProfilePicture((String) uploadResult.get("public_id"),
                    (String) uploadResult.get("asset_id"));
            profile.setProfilePicture(profilePicture);
            profilePicture.setAssociatedProfile(profile);
        }
        return profile;
    }
}
