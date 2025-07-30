package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "chats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private UUID id;

    @NonNull
    @Column(nullable = false, name = "initiator_name")
    private String initiatorUsername;

    @NonNull
    @Column(nullable = false, name = "recipient_name")
    private String recipientUsername;

    //TODO: There might be a better data structure (NavigableSet) instead of just a plain list for this.
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat")
    private List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    private LocalDateTime createdAt = LocalDateTime.now();
}
