package com.rebuild.backend.model.entities.profile_entities;

import com.rebuild.backend.utils.GenerateV7UUID;
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
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "public_id", nullable = false)
    @NonNull
    private String public_id;

    @Column(name = "asset_id", nullable = false)
    @NonNull
    private String asset_id;
}
