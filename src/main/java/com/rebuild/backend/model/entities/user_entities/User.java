package com.rebuild.backend.model.entities.user_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.resume_entities.Resume;
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

    @Serial
    private static final long serialVersionUID = 8L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @Column(name = "backup_username")
    @NonNull
    private String anonymizedName;

    @OneToOne(orphanRemoval = true, mappedBy = "user", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE
    })
    @JsonIgnore
    private UserProfile userProfile;

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

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void addChatParticipation(ChatParticipation participation)
    {
        this.chatParticipations.add(participation);
    }
}
