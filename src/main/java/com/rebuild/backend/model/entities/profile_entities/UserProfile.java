package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.*;


@Data
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles", indexes = {
        @Index(columnList = "user_id"),
        @Index(columnList = "picture_id")
})
public class UserProfile{


    //By default, no one other than the user themselves can see comments, posts and sensitive information.
    private static final InformationVisibility DEFAULT_COMMENTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_POSTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_SENSITIVE_INFO_VISIBILITY = InformationVisibility.NO_ONE;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(orphanRemoval = true, cascade = ALL)
    @JoinColumn(name = "picture_id", referencedColumnName = "id")
    private ProfilePicture profilePicture = null;

    @Column(name = "post_history_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility postsVisibility = DEFAULT_POSTS_VISIBILITY;

    @Column(name = "comment_history_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility commentsVisibility = DEFAULT_COMMENTS_VISIBILITY;

    @Column(name = "exclusive_friend_messages")
    private boolean messagesFromFriendsOnly = true;

    @Column(name = "sensitive_information_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility sensitiveInfoVisibility = DEFAULT_SENSITIVE_INFO_VISIBILITY;

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "associatedProfile")
    private List<ForumPost> madePosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            mappedBy = "associatedProfile", fetch = FetchType.LAZY)
    private List<Comment> madeComments = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            PERSIST,
            MERGE,
            REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;

}
