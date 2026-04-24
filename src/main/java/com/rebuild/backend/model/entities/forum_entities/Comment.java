package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "comments", indexes = {
        @Index(columnList = "author_id"),
        @Index(columnList = "post_id"),
        @Index(columnList = "parent_comment_id, creation_date")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private ForumPost associatedPost;

    @Column(name = "parent_comment_id")
    private UUID parentCommentId = null;

    private int repliesCount = 0;

    @Column(name = "creation_date")
    private Instant creationDate = Instant.now();

    private Instant modificationDate = Instant.now();

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String content;

    @Column(name = "likes_count", nullable = false)
    private int likeCount = 0;

    private boolean isDeleted = false;
}
