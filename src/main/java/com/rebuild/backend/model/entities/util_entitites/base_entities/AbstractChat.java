package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Message;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "is_group_chat", discriminatorType = DiscriminatorType.INTEGER)
@Data
public abstract class AbstractChat extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY)
    @OrderBy(value = "createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "participatedChat", fetch = FetchType.LAZY)
    @NonNull
    private List<ChatParticipation> participations = new ArrayList<>();

    @Column(name = "last_message")
    private String lastMessage = null;


}
