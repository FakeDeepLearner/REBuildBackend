package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "friendships", indexes = {
        @Index(columnList = "sender_id, recipient_id"),
        @Index(columnList = "recipient_id")
}, uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_id, recipient_id"})
})
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class FriendRelationship extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @NonNull
    private User sender;

    @NonNull
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User recipient;
}
