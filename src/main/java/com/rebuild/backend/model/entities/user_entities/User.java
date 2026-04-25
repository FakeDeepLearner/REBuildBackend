package com.rebuild.backend.model.entities.user_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.ChatParticipation;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static jakarta.persistence.CascadeType.ALL;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = {"email"}),
        //Even if we set up a unique constraint on phone numbers, postgresql allows for multiple null values
        @UniqueConstraint(name = "uk_phone_number", columnNames = {"phone_number"}),
        @UniqueConstraint(name = "uk_forum_username", columnNames = {"forum_username"})
}, indexes = {
        @Index(columnList = "lastLoginTime"),
        @Index(columnList = "email"),
        @Index(columnList = "phone_number")
})
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails, OidcUser, Serializable {

    @Serial
    private static final long serialVersionUID = 8L;

    private static final int MONTHS_ALLOWED_BEFORE_EXPIRY = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(
            nullable = false,
            name = "password"
    )
    @NonNull
    @JsonIgnore
    private String password;

    @Column(
            nullable = false,
            name = "email"
    )
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @NonNull
    @Column(name = "salt_value", nullable = false, unique = true)
    private String saltValue;

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
    @OrderBy("creationTime ASC")
    private List<Resume> resumes = new ArrayList<>();

    @Column(name = "is_mfa_user")
    private boolean enrolledInMFA = false;

    @Column(name = "mfa_secret_value")
    private String mfaSecretValue = null;

    @OneToMany(orphanRemoval = true, mappedBy = "user", cascade = ALL)
    @JsonIgnore
    private List<MFARecoveryCodeEntity> recoveryCodes;

    @Column(name = "forum_username")
    private String forumUsername;

    @Column(name = "backup_forum_username", nullable = false)
    private String anonymizedNameBase;

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

    @JsonIgnore
    private boolean accountNonLocked = false;

    @JsonIgnore
    private boolean credentialsNonExpired = false;

    @JsonIgnore
    private boolean enabled = false;

    @JsonIgnore
    private Instant signUpTime = Instant.now();

    @JsonIgnore
    private Instant lastLoginTime = Instant.now();

    public User(@NonNull String encodedPassword,
                @NonNull String email,
                String phoneNumber,
                @NonNull String saltValue) {
        this.password = encodedPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.saltValue = saltValue;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.NO_AUTHORITIES;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return lastLoginTime.isAfter(Instant.now().minus(MONTHS_ALLOWED_BEFORE_EXPIRY, ChronoUnit.MONTHS));
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

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


    @Override
    public Map<String, Object> getClaims() {
        return Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return null;
    }

    @Override
    public OidcIdToken getIdToken() {
        return null;
    }

    @Override
    public String getName() {
        return id.toString();
    }


    public String getForumUsername() {
        if  (forumUsername == null) return anonymizedNameBase;
        return forumUsername;
    }
}
