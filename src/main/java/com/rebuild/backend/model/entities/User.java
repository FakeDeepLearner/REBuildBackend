package com.rebuild.backend.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.enums.Authority;
import com.rebuild.backend.model.entities.resume_entities.PhoneNumber;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_email", columnNames = {"email"}),
        //Even if we set up a unique constraint on phone numbers, postgresql allows for multiple null values
        @UniqueConstraint(name = "uk_phone_number", columnNames = {"phone_number"})
}, indexes = {@Index(columnList = "lastLoginTime")})
@RequiredArgsConstructor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    private static final int MAX_RESUME_LIMIT = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;



    @Column(
            nullable = false,
            name = "password"
    )
    @NonNull
    private String password;


    @Column(
            nullable = false,
            name = "email"
    )
    @NonNull
    private String email;

    @Column(name = "phone_number")
    @NonNull
    private PhoneNumber phoneNumber;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Resume> resumes;

    @JsonIgnore
    private int numberOfResumes = 0;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Authority authority = Authority.USER_FREE;

    @JsonIgnore
    private boolean accountNonExpired = false;

    @JsonIgnore
    private boolean accountNonLocked = false;

    @JsonIgnore
    private boolean credentialsNonExpired = false;

    @JsonIgnore
    private boolean enabled = false;

    @JsonIgnore
    private LocalDateTime signUpTime = LocalDateTime.now();

    @JsonIgnore
    private LocalDateTime lastLoginTime = LocalDateTime.now();

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
        return accountNonExpired;
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
        return authority.equals(Authority.USER_FREE) && numberOfResumes == MAX_RESUME_LIMIT;
    }
}
