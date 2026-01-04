package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

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
public class FriendRelationship {

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

    @Column(name = "creation_time", nullable = false)
    private Instant friendshipTime =  Instant.now();
}
