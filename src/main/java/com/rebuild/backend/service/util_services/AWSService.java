package com.rebuild.backend.service.util_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.ResumeFileUploadRecord;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.exceptions.FileUploadException;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AWSService {

    private final Dotenv dotenv;

    private final S3AsyncClient s3AsyncClient;

    @Autowired
    public AWSService(Dotenv dotenv, S3AsyncClient s3AsyncClient) {
        this.dotenv = dotenv;
        this.s3AsyncClient = s3AsyncClient;
    }

    private String determineFileKey(User user, UUID randomId, String originalFileName) {
        return user.getId().toString() + "/" + randomId.toString() + "/" + originalFileName;
    }

    public CompletableFuture<Void> uploadFileToS3(String originalFileName,
                                                   User uploadingUser, ForumPost post,
                                                   byte[] fileBytes) {
        String objectKey = determineFileKey(uploadingUser, UUID.randomUUID(), originalFileName);
        String bucketName = dotenv.get("AWS_S3_BUCKET_NAME");

        PutObjectRequest putObjectRequest = PutObjectRequest.builder().
                bucket(bucketName).
                key(objectKey).build();

        AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(fileBytes);


        return s3AsyncClient.putObject(putObjectRequest, requestBody).
                thenAccept(putObjectResponse -> {
                    ResumeFileUploadRecord newUploadRecord = new ResumeFileUploadRecord(bucketName, objectKey,
                            putObjectResponse.expiration(), putObjectResponse.eTag());
                    newUploadRecord.setAssociatedPost(post);

                }).exceptionally(throwable -> {
                    throw new FileUploadException(throwable.getMessage(), throwable);
                });
    }

    public CompletableFuture<Void> deleteFileFromS3(ResumeFileUploadRecord uploadRecord)
    {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(uploadRecord.getBucketName())
                .key(uploadRecord.getObjectKey())
                .ifMatch(uploadRecord.getETag())
                .build();

        return s3AsyncClient.deleteObject(deleteObjectRequest).
                exceptionally(throwable ->  {
                    throw new FileUploadException(throwable.getMessage(), throwable);
                }).thenApply(_ -> null);
    }


    /* Create a pre-signed URL to download an object in a subsequent GET request.
     * This is pretty much straight up copied from the AWS code samples */
    public String createPresignedGetUrl(String bucketName, String objectKey) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(20))
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toExternalForm();
        }
    }
}
