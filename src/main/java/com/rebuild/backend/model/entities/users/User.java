package com.rebuild.backend.model.entities.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Chat;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    private static final int FREE_MAX_RESUME_LIMIT = 25;

    private static final int MONTHS_ALLOWED_BEFORE_EXPIRY = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    @JsonIgnore
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
    @Column(name = "salt_value", nullable = false)
    private String saltValue;

    @Column(name = "time_zone", nullable = false)
    @NonNull
    private ZoneId timeZone = ZoneId.of("UTC");

    @OneToOne(orphanRemoval = true, mappedBy = "associatedUser",
    cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE
    })
    @JsonIgnore
    private Inbox inbox;

    @OneToOne(orphanRemoval = true, mappedBy = "user", cascade = {
            CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE
    })
    @JsonIgnore
    private UserProfile profile = null;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true,
    cascade = {
            CascadeType.REMOVE,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @OrderColumn(name = "insertion_order")
    private List<Resume> resumes = new ArrayList<>();

    @Convert(converter = DatabaseEncryptor.class)
    @Column(name = "forum_username", nullable = false)
    private String forumUsername;

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            fetch = FetchType.LAZY, mappedBy = "creatingUser")
    private List<ForumPost> madePosts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
            mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> madeComments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL,
    mappedBy = "author", fetch = FetchType.LAZY)
    private List<CommentReply> madeReplies = new ArrayList<>();

    @JsonIgnore
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Chat> chats = new ArrayList<>();

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
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime signUpTime = LocalDateTime.now();

    @JsonIgnore
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime lastLoginTime = LocalDateTime.now();

    public User(@NonNull String encodedPassword,
                @NonNull String email,
                String phoneNumber,
                String forumUsername,
                @NonNull String saltValue,
                @NonNull ZoneId timeZone) {
        this.password = encodedPassword;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.forumUsername = forumUsername;
        this.saltValue = saltValue;
        this.timeZone = timeZone;
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
        return lastLoginTime.isAfter(LocalDateTime.now().minusMonths(MONTHS_ALLOWED_BEFORE_EXPIRY));
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

    public boolean maxResumeLimitReached(){
        return authority.equals(Authority.USER_FREE) && numberOfResumes == FREE_MAX_RESUME_LIMIT;
    }

    public String stringifiedNumber(){
        return this.phoneNumber;
    }

}
