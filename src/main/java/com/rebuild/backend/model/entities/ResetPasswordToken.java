package com.rebuild.backend.model.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "reset_tokens")
public class ResetPasswordToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(nullable = false, name = "reset_token")
    private String actualToken;

    @NonNull
    @Column(name = "email", nullable = false)
    private String emailFor;

    @NonNull
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiry;


}
