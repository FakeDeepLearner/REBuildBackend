package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "comments")
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
    private UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id")
    @JsonIgnore
    private User author;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private ForumPost associatedPost;

    @OneToMany(mappedBy = "topLevelComment", orphanRemoval = true,
            cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommentReply> replies = new ArrayList<>();

    private int repliesCount = 0;

    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @JsonIgnore
    private LocalDateTime creationDate = LocalDateTime.now();

    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    @JsonIgnore
    private LocalDateTime modificationDate = LocalDateTime.now();

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String content;

    @Column(name = "likes_count", nullable = false)
    private int likeCount = 0;

    @NonNull
    private String authorUsername;
}
