package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
}
