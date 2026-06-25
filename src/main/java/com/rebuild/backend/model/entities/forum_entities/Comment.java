package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
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
public class Comment extends Auditable {

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

    // This can be another comment if this comment is a reply to that comment,
    // or it can be the id of this comment's associated post
    // if this is a top-level comment
    @Column(name = "parent_comment_id")
    private UUID parentId;

    @Column(name = "replies_count")
    private int repliesCount = 0;

    @NonNull
    private String content;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "is_anonymized")
    private boolean isAnonymized = false;

    @Column(name = "is_edited")
    private boolean isEdited = false;


}
