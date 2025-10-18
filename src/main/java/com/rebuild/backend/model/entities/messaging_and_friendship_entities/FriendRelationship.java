package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.GenerateV7UUID;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friendships", indexes = {
        @Index(columnList = "sender_id"),
        @Index(columnList = "recipient_id")
})
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class FriendRelationship {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    @NonNull
    private User sender;

    @NonNull
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    @ManyToOne
    private User recipient;


    private LocalDateTime friendshipTime =  LocalDateTime.now();
}
