package com.rebuild.backend.model.entities.user_entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "temporary_mfa_secrets", indexes = {
        @Index(columnList = "email")
})
@RequiredArgsConstructor
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemporaryMFACredentials {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(unique = true)
    private String email;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "temporary_codes", joinColumns = @JoinColumn(name = "code_id", referencedColumnName = "id"))
    private List<String> temporaryCodes;

    @NonNull
    private String secret;

    @NonNull
    private Instant expiryTime;
}
