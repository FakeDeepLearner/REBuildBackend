package com.rebuild.backend.model.entities.messaging_and_friendship_entities;


import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "0")
@Data
@NoArgsConstructor
@Entity
public class PrivateChat extends AbstractChat {

    public PrivateChat(User sender, User recipient, String initialMessageContent)
    {
        ChatParticipation senderParticipation = new ChatParticipation(sender, this,
                false, true, true);
        senderParticipation.setParticipatingUser(sender);
        sender.addChatParticipation(senderParticipation);
        senderParticipation.setLastMessage(initialMessageContent);

        ChatParticipation recipientParticipation = new ChatParticipation(recipient, this,
              false, true, false);
        recipientParticipation.setParticipatingUser(sender);
        recipient.addChatParticipation(recipientParticipation);
        recipientParticipation.setUnreadMessagesCount(1);
        recipientParticipation.setLastMessage(initialMessageContent);

        this.setParticipations(new ArrayList<>(List.of(senderParticipation, recipientParticipation)));
    }

}
