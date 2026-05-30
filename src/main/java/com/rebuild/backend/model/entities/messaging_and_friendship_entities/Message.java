package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "messages", indexes = {
        @Index(columnList = "sender_id"),
        @Index(columnList = "recipient_id"),
        @Index(columnList = "associated_chat_id")
})
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Message extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @NonNull
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "associated_chat_id")
    @JsonIgnore
    private AbstractChat associatedChat;

    @NonNull
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted")
    private boolean isRemoved = false;


    public String getDisplayedContent()
    {
        return isRemoved ? "This message has been removed" : content;
    }
}
