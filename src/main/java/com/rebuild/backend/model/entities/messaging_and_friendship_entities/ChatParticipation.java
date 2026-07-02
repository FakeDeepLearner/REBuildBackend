package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "chat_participations", indexes = {
        @Index(columnList = "participating_user_id, participating_chat_id"),
        @Index(columnList = "participating_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatParticipation extends Auditable {

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
    private AbstractChat participatedChat;

    @NonNull
    @Column(name = "is_group_chat")
    private Boolean isGroupChat;


    // Indicates whether this user has muted this chat or not.
    @Column(name = "is_muted")
    private boolean isMuted = false;

    @Column(name = "num_unread_messages")
    private int unreadMessagesCount = 0;

    @Column(name = "last_message")
    private String lastMessage;

    // Only the creator of the group chat is permanently an admin.
    // While these fields will also exist for private chats, they will have no meaning there.
    @Column(name = "is_admin")
    @NonNull
    private Boolean isAdmin;

    public boolean hasNoAdminPrivileges()
    {
        return !isAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChatParticipation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
