package com.rebuild.backend.model.entities.chat_entities;


import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.AbstractChat;
import com.rebuild.backend.utils.UserPair;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "private_chats",
    indexes = {
        @Index(columnList = "low_id, high_id")
    })
public class PrivateChat extends AbstractChat {

    @Column(name = "low_id")
    private UUID lowUserId;

    @Column(name = "high_id")
    private UUID highUserId;

    public PrivateChat(User sender, User recipient, String initialMessageContent)
    {
        super(2, 2);
        UserPair userPair = new UserPair(sender, recipient);
        this.lowUserId = userPair.lowId();
        this.highUserId = userPair.highId();

        ChatParticipation senderParticipation = new ChatParticipation(sender, this,
                true);
        senderParticipation.setParticipatingUser(sender);
        sender.addChatParticipation(senderParticipation);
        senderParticipation.setLastMessage(initialMessageContent);

        ChatParticipation recipientParticipation = new ChatParticipation(recipient, this,
              true);
        recipientParticipation.setParticipatingUser(sender);
        recipient.addChatParticipation(recipientParticipation);
        recipientParticipation.setUnreadMessagesCount(1);
        recipientParticipation.setLastMessage(initialMessageContent);

        this.setParticipations(new ArrayList<>(List.of(senderParticipation, recipientParticipation)));
    }

}
