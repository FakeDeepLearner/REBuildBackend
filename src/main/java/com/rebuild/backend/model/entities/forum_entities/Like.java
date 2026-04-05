package com.rebuild.backend.model.entities.forum_entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "liked_object_id, liking_user_id")
}, indexes = {
        @Index(columnList = "liking_user_id, liked_object_id")
})
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Column(nullable = false, name = "liking_user_id")
    private UUID likingUserId;

    @NonNull
    @Column(nullable = false, name = "liked_object_id")
    private UUID likedObjectId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false, name = "liking_timestamp")
    private Instant likeTimestamp = Instant.now();
}
