package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractChat;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "1")
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GroupChat extends AbstractChat {

    @Column(name = "chat_name")
    @NonNull
    private String chatName;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "associatedChat")
    private GroupChatPicture picture;

    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatInvitation> invitations = new ArrayList<>();
}
