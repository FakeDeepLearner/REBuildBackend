package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.model.forms.chat_forms.NewChatForm;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "1")
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@Entity
public class GroupChat extends AbstractChat {

    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "description")
    private String chatDescription;

    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatInvitation> invitations = new ArrayList<>();

    public GroupChat(NewChatForm newChatForm) {
        super(1, 1);
        this.chatName = newChatForm.chatName();
        this.chatDescription = newChatForm.chatDescription();
    }
}
