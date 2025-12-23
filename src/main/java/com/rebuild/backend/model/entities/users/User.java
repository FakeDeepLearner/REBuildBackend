package com.rebuild.backend.model.entities.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.converters.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_email", columnNames = {"email"}),
        //Even if we set up a unique constraint on phone numbers, postgresql allows for multiple null values
        @UniqueConstraint(name = "uk_phone_number", columnNames = {"phone_number"}),
        @UniqueConstraint(name = "uk_forum_username", columnNames = {"forum_username"})
}, indexes = {
        @Index(columnList = "lastLoginTime")
})
@RequiredArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Indexed
public class User implements UserDetails, OidcUser {

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
    @OrderColumn(name = "insertion_order")
    private List<Resume> resumes = new ArrayList<>();


    @Column(name = "forum_username")
    @FullTextField
    private String forumUsername;


    @Column(name = "backup_forum_username", nullable = false)
    private String backupForumUsername;

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "creatingUser")
    private List<ForumPost> madePosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> madeComments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY,
    mappedBy = "initiatingUser")
    private List<Chat> initiatedChats = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY,
            mappedBy = "receivingUser")
    private List<Chat> receivedChats = new ArrayList<>();

    @JsonIgnore
    private int numberOfResumes = 0;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Authority authority = Authority.USER_FREE;

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
        SimpleGrantedAuthority freeAuthority = new SimpleGrantedAuthority(Authority.USER_FREE.name());
        SimpleGrantedAuthority paidAuthority = new SimpleGrantedAuthority(Authority.USER_PAID.name());
        if (authority == Authority.USER_FREE){
            return List.of(freeAuthority);
        }
        else{
            return List.of(freeAuthority, paidAuthority);
        }
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
        return Objects.hashCode(getId());
    }


    public void addSenderChat(Chat chat){
        initiatedChats.add(chat);
    }

    public void addReceiverChat(Chat chat){
        receivedChats.add(chat);
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
        if  (forumUsername == null) return backupForumUsername;
        return forumUsername;
    }
}
