package com.rebuild.backend.model.entities.profile_entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "profile_settings")
@AllArgsConstructor
@NoArgsConstructor
@Data
@RequiredArgsConstructor
public class ProfileSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 15L;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @Column(name = "public_post_history")
    @NonNull
    private Boolean publicPostHistory;

    @Column(name = "public_comment_history")
    @NonNull
    private Boolean publicCommentHistory;

    @Column(name = "exclusive_friend_messages")
    @NonNull
    private Boolean messagesFromFriendsOnly;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile associatedProfile;
}
