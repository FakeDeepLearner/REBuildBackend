package com.rebuild.backend.model.entities.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "captcha_verifications")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class CaptchaVerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Column(name = "ip_addr", nullable = false)
    private String ipAddress;

    @NonNull
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @NonNull
    @Column(name = "success", nullable = false)
    private Boolean successful;
}
