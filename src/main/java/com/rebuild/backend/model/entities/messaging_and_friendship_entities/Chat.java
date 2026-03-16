package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.*;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "chats", indexes = {
        @Index(columnList = "initiating_user_id"),
        @Index(columnList = "initiating_user_id, receiving_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "participatedChat", fetch = FetchType.LAZY)
    @NonNull
    private List<ChatParticipation> participations = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY)
    @OrderBy(value = "createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    @Column(name = "creation_time")
    private Instant createdAt = Instant.now();

    @Column(name = "last_message")
    private String lastMessage = null;

    //The name being null means that this is a chat between 2 users. Otherwise, it is a "group chat".
    @Column(name = "chat_name")
    private String chatName = null;

    @OneToOne(orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "picture_id", referencedColumnName = "id")
    private ProfilePicture chatPicture = null;

    //A chat only has invitations if it is a group chat.
    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatInvitation> invitations = new ArrayList<>();
}
