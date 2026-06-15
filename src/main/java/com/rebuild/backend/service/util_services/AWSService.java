package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractPicture;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.user_services.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class AWSService {

    private final S3Client s3Client;

    private final ProfileRepository profileRepository;

    private final ProfileService profileService;

    @Autowired
    public AWSService(S3Client s3Client, ProfileRepository profileRepository, ProfileService profileService) {
        this.s3Client = s3Client;
        this.profileRepository = profileRepository;
        this.profileService = profileService;
    }


    public String generateDownloadUrlForPicture(AbstractPicture picture) {
        try (S3Presigner presigner = S3Presigner.builder().credentialsProvider(() ->
                AwsBasicCredentials.create(System.getenv("AWS_ACCESS_KEY_ID"),
                        System.getenv("AWS_SECRET_ACCESS_KEY")
        )).build()){

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(picture.getBucketName())
                    .key(picture.getKeyName())
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toExternalForm();
        }
    }

    private void deletePicture(AbstractPicture picture) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.
                builder().bucket(picture.getBucketName()).key(picture.getKeyName()).build();

        s3Client.deleteObject(deleteObjectRequest);
    }


    public UserProfileResponse removeProfilePicture(User removingUser)
    {
        UserProfile profile = removingUser.getUserProfile();

        deletePicture(profile.getPicture());

        profile.setPicture(null);

        profileRepository.save(profile);

        return profileService.loadSelfProfile(removingUser);
    }

}
