package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.utils.UserPair;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "friendships", indexes = {
        @Index(columnList = "low_id, high_id")
})
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Friendship extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "low_id", nullable = false)
    @NonNull
    private UUID lowUserId;

    @Column(name = "high_id", nullable = false)
    @NonNull
    private UUID highUserId;

    public Friendship(UserPair userPair)
    {
        this.lowUserId = userPair.lowId();
        this.highUserId = userPair.highId();
    }
}
