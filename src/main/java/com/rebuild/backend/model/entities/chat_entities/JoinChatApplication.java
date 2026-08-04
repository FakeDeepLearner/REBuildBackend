package com.rebuild.backend.model.entities.chat_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "join_chat_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class JoinChatApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String content;

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "applying_user_id", referencedColumnName = "id")
    @NonNull
    private User associatedUser;

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "associated_chat_id", referencedColumnName = "id")
    @NonNull
    private GroupChat associatedChat;
}
