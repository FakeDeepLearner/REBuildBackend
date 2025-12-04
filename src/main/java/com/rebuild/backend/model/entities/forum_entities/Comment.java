package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
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
@NamedQueries(
        value = {
                @NamedQuery(name = "Comment.countByIdAndUserId",
                query = "SELECT COUNT(*) FROM Comment c WHERE c.id=?1 and c.author.id=?2")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private ForumPost associatedPost;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
                CascadeType.MERGE, CascadeType.PERSIST
    })
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnore
    private Comment parent;

    private int repliesCount = 0;

    @Column(name = "creation_date")
    @JsonIgnore
    private Instant creationDate = Instant.now();

    @JsonIgnore
    private Instant modificationDate = Instant.now();

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String content;

    @Column(name = "likes_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "author_name")
    private String authorUsername;


    @JsonGetter(value = "authorUsername")
    private String determineAuthorName()
    {
        return authorUsername != null ? authorUsername : "Anonymous";
    }
}
