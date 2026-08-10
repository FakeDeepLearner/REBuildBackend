package com.rebuild.backend.model.entities.user_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.dtos.auth_dtos.ClerkInformation;
import com.rebuild.backend.model.entities.chat_entities.JoinChatApplication;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.chat_entities.ChatParticipation;
import com.rebuild.backend.model.enums.InformationVisibility;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;

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
    private String imageUrl;

    @Column(name = "forum_username", unique = true)
    @NonNull
    private String forumUsername;

    @Column(name = "location")
    private String location = null;

    @Column(name = "bio")
    private String biography = null;

    @Column(name = "sensitive_information_setting")
    @Enumerated(EnumType.STRING)
    private InformationVisibility sensitiveInfoVisibility = DEFAULT_SENSITIVE_INFO_VISIBILITY;

    @OneToMany(orphanRemoval = true, cascade = ALL, mappedBy = "participatingUser",
    fetch = FetchType.LAZY)
    private List<ChatParticipation> chatParticipations = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = ALL, mappedBy = "associatedUser",
    fetch = FetchType.LAZY)
    private List<JoinChatApplication> chatApplications = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "user")
    private List<ForumPost> madePosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            mappedBy = "user", fetch = FetchType.LAZY)
    private List<Comment> madeComments = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    public void update(ClerkInformation clerkInformation)
    {
        this.email = StringUtil.findPrimaryEmail(clerkInformation);
        this.name = clerkInformation.name();
        this.forumUsername = clerkInformation.username();
        this.imageUrl = clerkInformation.hasImage() ? clerkInformation.imageUrl() : null;
    }

    public User(ClerkInformation clerkInformation)
    {
        String primaryEmail = StringUtil.findPrimaryEmail(clerkInformation);

        this(clerkInformation.name(),
                primaryEmail, clerkInformation.id(),
                clerkInformation.username());

        this.imageUrl = clerkInformation.hasImage() ? clerkInformation.imageUrl() : null;

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void addChatParticipation(ChatParticipation participation)
    {
        this.chatParticipations.add(participation);
    }

    public void addChatApplication(JoinChatApplication chatApplication)
    {
        this.chatApplications.add(chatApplication);
    }

    @Override
    public String toString() {
        return this.clerkId;
    }
}
