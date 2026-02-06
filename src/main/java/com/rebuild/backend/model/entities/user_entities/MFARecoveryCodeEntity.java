package com.rebuild.backend.model.entities.user_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "mfa_codes", indexes = {
        @Index(columnList = "user_id")
})
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class MFARecoveryCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(name = "code")
    private String hashedCode;


    @ManyToOne(cascade = {CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "code_is_used")
    private boolean used = false;
}
