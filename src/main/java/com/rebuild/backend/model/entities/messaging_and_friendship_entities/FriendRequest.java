package com.rebuild.backend.model.entities.messaging_and_friendship_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.utils.UserPair;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friend_requests", indexes = {
        @Index(columnList = "low_id, high_id"),
        @Index(columnList = "id, recipient_id"),
        @Index(columnList = "creation_time DESC")
})
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class FriendRequest extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
    @NonNull
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = false)
    @NonNull
    private User recipient;

    private String content;

    public FriendRequest(User sender, User recipient, String content)
    {
        this(sender, recipient);
        if (content.isBlank())
        {
            this.content = null;
        }
        else
        {
            this.content = content;
        }
    }

}

