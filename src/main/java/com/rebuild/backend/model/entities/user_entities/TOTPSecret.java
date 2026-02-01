package com.rebuild.backend.model.entities.user_entities;

import com.rebuild.backend.utils.converters.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "totp_secrets")
@NoArgsConstructor
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class TOTPSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SecretStatus status;

    @Column(name = "user_secret")
    @Convert(converter = DatabaseEncryptor.class)
    @NonNull
    private String actualSecret;


    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
