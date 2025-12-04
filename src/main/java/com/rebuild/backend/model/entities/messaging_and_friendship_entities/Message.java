package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;

import java.time.Instant;
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
@Indexed
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    @GenericField
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id")
    @NonNull
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id")
    @NonNull
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "associated_chat_id")
    @JsonIgnore
    private Chat associatedChat;

    @NonNull
    @Column(name = "content", nullable = false)
    @FullTextField
    private String content;

    @Column(name = "creationDate")
    private Instant createdAt = Instant.now();
}
