package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.print.attribute.standard.MediaSize;
import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatParticipation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "participating_user_id", referencedColumnName = "id")
    @JsonIgnore
    @NonNull
    private User participatingUser;


    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinColumn(name = "participated_chat_id", referencedColumnName = "id")
    @JsonIgnore
    @NonNull
    private GroupChat participatedChat;


    // Indicates whether this user has muted this chat or not.
    @Column(name = "is_muted")
    private boolean isMuted = false;

    @Column(name = "num_unread_messages")
    private int unreadMessagesCount = 0;



}
