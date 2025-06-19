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
@RequiredArgsConstructor
@Data
@Table(name = "post_likes", uniqueConstraints =
        {
                @UniqueConstraint(name = "uk_unique_post_like", columnNames = {"liked_post_id", "user_email"})
        }, indexes = {
        @Index(name = "post_user_index", columnList = "liked_post_id, user_email")
})
@EntityListeners(AuditingEntityListener.class)
@NamedQueries(
        value = {
                @NamedQuery(name = "PostLike.countByPostIdAndEmail",
                query = "SELECT COUNT(*) FROM PostLike l WHERE l.likedPostID=?1 AND l.likingUserEmail=?2")
        }
)
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "liked_post_id", nullable = false)
    @NonNull
    private UUID likedPostID;


    @Column(name = "user_email", nullable = false)
    @NonNull
    private String likingUserEmail;

    @CreatedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime creationDate = LocalDateTime.now();
}
