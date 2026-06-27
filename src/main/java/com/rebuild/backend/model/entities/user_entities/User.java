package com.rebuild.backend.model.entities.user_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.dtos.ClerkEmail;
import com.rebuild.backend.model.dtos.ClerkInformation;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "clerk_id"),
        @Index(columnList = "backup_username")
})
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    //By default, no one other than the user themselves can see comments, posts and sensitive information.
    private static final InformationVisibility DEFAULT_COMMENTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_POSTS_VISIBILITY = InformationVisibility.NO_ONE;

    private static final InformationVisibility DEFAULT_SENSITIVE_INFO_VISIBILITY = InformationVisibility.NO_ONE;

    @Serial
    private static final long serialVersionUID = 8L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String name;

    @Column(
            nullable = false,
            name = "email",
            unique = true
    )
    @NonNull
    private String email;

    @Column(name = "clerk_id")
    @NonNull
    private String clerkId;

    @Column(name = "image_url")
    @NonNull
    private String imageUrl;

    @Column(name = "forum_username", unique = true)
    @NonNull
    private String forumUsername;

    @Column(name = "phone_number")
    private String phoneNumber;

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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true,
    cascade = {
            CascadeType.REMOVE,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @OrderBy("createdAt ASC")
    private List<Resume> resumes = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = ALL, mappedBy = "participatingUser",
    fetch = FetchType.LAZY)
    private transient List<ChatParticipation> chatParticipations = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "user")
    private List<ForumPost> madePosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            mappedBy = "user", fetch = FetchType.LAZY)
    private List<Comment> madeComments = new ArrayList<>();

    @JsonIgnore
    private int numberOfResumes = 0;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    public void update(ClerkInformation clerkInformation)
    {
        this.email = StringUtil.findPrimaryEmail(clerkInformation);
        this.phoneNumber = StringUtil.findPrimaryPhoneNumber(clerkInformation);
        this.name = clerkInformation.name();
        this.forumUsername = clerkInformation.username();
        this.imageUrl = clerkInformation.imageUrl();
    }

    public User(ClerkInformation clerkInformation)
    {
        String primaryEmail = StringUtil.findPrimaryEmail(clerkInformation);

        String primaryPhoneNumber = StringUtil.findPrimaryPhoneNumber(clerkInformation);

        this(clerkInformation.name(),
                primaryEmail, clerkInformation.id(),
                clerkInformation.imageUrl(), clerkInformation.username());

        this.phoneNumber = primaryPhoneNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void addChatParticipation(ChatParticipation participation)
    {
        this.chatParticipations.add(participation);
    }
}
