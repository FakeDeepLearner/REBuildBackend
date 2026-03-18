package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "is_group_chat", discriminatorType = DiscriminatorType.INTEGER)
@Data
public class AbstractChat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY)
    @OrderBy(value = "createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    @Column(name = "creation_time")
    private Instant createdAt = Instant.now();

    @Column(name = "last_message")
    private String lastMessage = null;


}
