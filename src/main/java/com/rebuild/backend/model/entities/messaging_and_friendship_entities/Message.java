package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.utils.GenerateV7UUID;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

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
public class Message {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "associated_chat_id")
    @JsonIgnore
    private Chat associatedChat;

    @NonNull
    @Column(name = "content", nullable = false)
    private String content;

    private LocalDateTime createdAt = LocalDateTime.now();
}
