package com.rebuild.backend.model.entities.chat_entities;

import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.enums.ChatStatus;
import com.rebuild.backend.model.forms.chat_forms.NewChatForm;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@Entity
@Table(name = "group_chats")
public class GroupChat extends AbstractChat {

    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "description")
    private String chatDescription;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ChatStatus chatStatus = ChatStatus.INVITE_ONLY;

    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatInvitation> invitations = new ArrayList<>();

    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JoinChatApplication> applications = new ArrayList<>();

    public GroupChat(NewChatForm newChatForm) {
        super(1, 1);
        this.chatName = newChatForm.chatName();
        this.chatDescription = newChatForm.chatDescription();
    }
}
