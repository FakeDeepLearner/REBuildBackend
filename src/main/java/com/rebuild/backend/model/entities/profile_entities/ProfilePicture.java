package com.rebuild.backend.model.entities.profile_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "profile_pictures", indexes = {
        @Index(columnList = "asset_id")
})
public class ProfilePicture {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "public_id", nullable = false)
    @NonNull
    private String public_id;

    @Column(name = "asset_id", nullable = false)
    @NonNull
    private String asset_id;

    @Column(name = "secure_url", nullable = false)
    @NonNull
    private String secure_url;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    private UserProfile associatedProfile;
}
