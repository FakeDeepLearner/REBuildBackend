package com.rebuild.backend.model.entities.user_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "temporary_mfa_secrets", indexes = {
        @Index(columnList = "email")
})
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryMFASecret {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(unique = true)
    private String email;

    @NonNull
    private String secret;
}
