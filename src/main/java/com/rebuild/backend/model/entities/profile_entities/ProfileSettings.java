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
public class ProfileSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 15L;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @Column(name = "public_post_history")
    private boolean publicPostHistory;

    @Column(name = "public_comment_history")
    private boolean publicCommentHistory;

    @Column(name = "exclusive_friend_messages")
    private boolean messagesFromFriendsOnly;

    @Column(name = "sensitive_information_setting")
    @Enumerated(EnumType.STRING)
    private SensitiveInformationVisibility sensitiveInformationVisibility;
    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile associatedProfile;

    public static ProfileSettings defaultSettings()
    {
        return new ProfileSettings(false, false, false,
                SensitiveInformationVisibility.NO_ONE);
    }

    public ProfileSettings(boolean publicPostHistory,
                           boolean publicCommentHistory, boolean messagesFromFriendsOnly,
                           SensitiveInformationVisibility sensitiveInformationVisibility)
    {
        this.messagesFromFriendsOnly = messagesFromFriendsOnly;
        this.publicCommentHistory = publicCommentHistory;
        this.publicPostHistory = publicPostHistory;
        this.sensitiveInformationVisibility = sensitiveInformationVisibility;
    }

}
