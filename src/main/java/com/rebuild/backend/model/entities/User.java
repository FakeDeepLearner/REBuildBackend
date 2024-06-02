package com.rebuild.backend.model.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
        @UniqueConstraint(name = "uk_email", columnNames = {"email"})
})
@RequiredArgsConstructor
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(
            nullable = false,
            name = "username"
    )
    @NonNull
    private String username;


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

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Resume> resumes;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Authority authority = Authority.USER_FREE;

    @Transient
    @JsonIgnore
    private boolean accountNonExpired = false;

    @Transient
    @JsonIgnore
    private boolean accountNonLocked = false;

    @Transient
    @JsonIgnore
    private boolean credentialsNonExpired = false;

    @Transient
    @JsonIgnore
    private boolean enabled = true;

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
}
