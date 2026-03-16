package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import javax.print.attribute.standard.MediaSize;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "participating_user_id", referencedColumnName = "id")
    @JsonIgnore
    @NonNull
    private User participatingUser;


    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "participated_chat_id", referencedColumnName = "id")
    @JsonIgnore
    @NonNull
    private Chat participatedChat;

    // Indicates whether this user initiated this chat (i.e. sent the first message),
    // or received this chat (i.e. received the first message)
    @NonNull
    @Column(name = "is_sender")
    private Boolean isSender;

    // Indicates whether this user has muted this chat or not.
    @Column(name = "is_muted")
    private boolean isMuted = false;

    @Column(name = "joined_at")
    private Instant initiatedTime = Instant.now();

    @Column(name = "num_unread_messages")
    private int unreadMessagesCount = 0;



}
