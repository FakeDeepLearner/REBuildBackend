package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue(value = "1")
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GroupChat extends AbstractChat {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "participatedChat", fetch = FetchType.LAZY)
    @NonNull
    private List<ChatParticipation> participations = new ArrayList<>();

    @Column(name = "chat_name")
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String chatName;

    @Column(name = "profile_picture_id")
    @Convert(converter = DatabaseEncryptor.class)
    private String pictureId = null;

    @OneToMany(mappedBy = "associatedChat", orphanRemoval = true,
            cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatInvitation> invitations = new ArrayList<>();
}
