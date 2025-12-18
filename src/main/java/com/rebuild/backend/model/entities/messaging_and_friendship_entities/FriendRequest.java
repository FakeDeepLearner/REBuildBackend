package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.Inbox;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friend_requests", indexes = {
        @Index(columnList = "sender_id, recipient_id"),
        @Index(columnList = "status")
})
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "creation_time")
    private Instant creationTimestamp = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
    @NonNull
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = false)
    @NonNull
    private User recipient;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "inbox_id", referencedColumnName = "id", nullable = false)
    @NonNull
    private Inbox associatedInbox;
}

