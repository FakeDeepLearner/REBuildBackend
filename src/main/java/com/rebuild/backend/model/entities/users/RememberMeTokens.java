package com.rebuild.backend.model.entities.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "persistent_logins", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "token"}, name = "uk_persistent_username_token")
})
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class RememberMeTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String series;

    @Column(nullable = false)
    @NonNull
    private String username;

    @Column(nullable = false)
    @NonNull
    private String token;

    @Column(nullable = false, name = "last_used")
    @NonNull
    private LocalDateTime lastUsedTime;
}
