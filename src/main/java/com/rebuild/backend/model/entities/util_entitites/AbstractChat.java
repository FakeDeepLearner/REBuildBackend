package com.rebuild.backend.model.entities.util_entitites;

import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.entities.chat_entities.Message;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public abstract class AbstractChat extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY,
    orphanRemoval = true)
    @OrderBy(value = "createdAt DESC")
    private List<Message> messages = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "associatedChat", fetch = FetchType.LAZY,
    orphanRemoval = true)
    private List<ChatParticipation> participations = new ArrayList<>();

    @Column(name = "last_message")
    private String lastMessage = null;

    @NonNull
    @Column(name = "member_count", columnDefinition = "int")
    private Integer memberCount;

    @NonNull
    @Column(name = "member_count", columnDefinition = "int")
    private Integer administratorCount;

}
