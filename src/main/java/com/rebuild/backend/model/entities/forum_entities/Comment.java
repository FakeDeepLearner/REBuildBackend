package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.usertype.UserType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_id")
    private ForumPost associatedPost;

    @OneToMany(mappedBy = "topLevelComment", orphanRemoval = true,
            cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<CommentReply> replies = new ArrayList<>();

    private int repliesCount = 0;

    @CreatedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime creationDate = LocalDateTime.now();

    @LastModifiedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime modificationDate = LocalDateTime.now();

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String content;
}
