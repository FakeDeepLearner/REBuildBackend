package com.rebuild.backend.model.entities.messaging_and_friendship_entities.chat_entities;


import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "0")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@RequiredArgsConstructor
public class PrivateChat extends AbstractChat {

    @NonNull
    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private User sender;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id")
    private User recipient;

    @Column(name = "sender_unreads")
    private int senderUnreadMessages = 0;

    @Column(name = "recipient_unreads")
    private int recipientUnreadMessages = 0;

    public void addSenderUnread()
    {
        this.senderUnreadMessages++;
    }

    public void addRecipientUnread()
    {
        this.recipientUnreadMessages++;
    }


    public PrivateChat(User sender, User recipient, String firstMessage)
    {
        this.sender = sender;
        this.recipient = recipient;
        Message newMessage = new Message(this.sender, firstMessage);

        newMessage.setAssociatedChat(this);

        // This syntax is used, because the List.of
        // methods all return unmodifiable lists, which is not what we want
        this.messages = new ArrayList<>(List.of(newMessage));
        this.lastMessage = firstMessage;
        this.addRecipientUnread();

    }

}
