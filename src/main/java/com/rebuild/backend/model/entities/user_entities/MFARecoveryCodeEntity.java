package com.rebuild.backend.model.entities.user_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mfa_codes", indexes = {
        @Index(columnList = "user_id")
})
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class MFARecoveryCodeEntity extends Auditable {

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
