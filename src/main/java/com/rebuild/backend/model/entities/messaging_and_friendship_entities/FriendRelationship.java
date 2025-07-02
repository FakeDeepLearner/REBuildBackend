package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class FriendRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
