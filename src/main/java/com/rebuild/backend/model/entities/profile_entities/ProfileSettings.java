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

    //By default, no one other than the user themselves can see comments, posts and sensitive information.
    private static final InformationVisibility DEFAULT_COMMENTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_POSTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_SENSITIVE_INFO_VISIBILITY = InformationVisibility.NO_ONE;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @Column(name = "post_history_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility postsVisibility;

    @Column(name = "comment_history_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility commentsVisibility;

    @Column(name = "exclusive_friend_messages")
    private boolean messagesFromFriendsOnly;

    @Column(name = "sensitive_information_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility sensitiveInfoVisibility;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile associatedProfile;

    public static ProfileSettings defaultSettings()
    {
        return new ProfileSettings(DEFAULT_POSTS_VISIBILITY, DEFAULT_COMMENTS_VISIBILITY,
                false, DEFAULT_SENSITIVE_INFO_VISIBILITY);
    }

    private ProfileSettings(InformationVisibility postsVisibility,
                           InformationVisibility commentsVisibility, boolean messagesFromFriendsOnly,
                           InformationVisibility sensitiveInfoVisibility)
    {
        this.messagesFromFriendsOnly = messagesFromFriendsOnly;
        this.commentsVisibility = commentsVisibility;
        this.postsVisibility = postsVisibility;
        this.sensitiveInfoVisibility = sensitiveInfoVisibility;
    }

}
