package com.rebuild.backend.model.entities.forum_entities;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comment_likes", uniqueConstraints =
        {
                @UniqueConstraint(name = "uk_unique_comment_like",
                        columnNames = {"liked_comment_id", "user_email"})
        }, indexes = {
        @Index(name = "comment_user_index", columnList = "liked_comment_id, user_email")
})
@NamedQueries(
        value = {
                @NamedQuery(name = "CommentLike.countByPostIdAndEmail",
                        query = "SELECT COUNT(*) FROM CommentLike l WHERE l.likedCommentID=?1 AND l.likingUserEmail=?2")
        }
)
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "liked_comment_id", nullable = false)
    @NonNull
    private UUID likedCommentID;


    @Column(name = "user_email", nullable = false)
    @NonNull
    private String likingUserEmail;

    @CreatedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime creationDate = LocalDateTime.now();
}

