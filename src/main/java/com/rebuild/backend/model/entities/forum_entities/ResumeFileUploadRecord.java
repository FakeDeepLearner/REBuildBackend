package com.rebuild.backend.model.entities.forum_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "file_upload_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class ResumeFileUploadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bucket_name")
    @NonNull
    private String bucketName;

    @NonNull
    @Column(name = "object_key")
    private String objectKey;

    @NonNull
    @Column(name = "expiration")
    private String expiration;

    @NonNull
    @Column(name = "eTag")
    private String eTag;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "post_id", referencedColumnName = "id")
    private ForumPost associatedPost;
}
