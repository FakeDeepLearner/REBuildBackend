package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "chats", indexes = {
        @Index(columnList = "initiating_user_id"),
        @Index(columnList = "initiating_user_id, receiving_user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    //The actual names of these don't matter. It just matters that one is defined as the initiator.
    //This is necessary to make the mappings back in the User class more understandable.
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiating_user_id", referencedColumnName = "id")
    private User initiatingUser;

    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_user_id", referencedColumnName = "id")
    private User receivingUser;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY)
    @OrderBy(value = "createdAt ASC")
    private List<Message> messages = Collections.synchronizedList(new ArrayList<>());


    private Instant createdAt = Instant.now();
}
