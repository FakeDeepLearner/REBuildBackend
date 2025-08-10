package com.rebuild.backend.model.entities.forum_entities;

import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "liked_object_id, liking_username")
}, indexes = {
        @Index(columnList = "liking_username, liked_object_type")
})
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false, name = "liking_username")
    private String likingUsername;

    @NonNull
    @Column(nullable = false, name = "liked_object_id")
    private UUID likedObjectId;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "liked_object_type")
    private LikeType likedObjectType;

    @CreationTimestamp
    @NonNull
    @Column(nullable = false, updatable = false, name = "liking_timestamp")
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime likedDateTime;
}
