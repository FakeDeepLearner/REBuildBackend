package com.rebuild.backend.model.entities.profile_entities;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeVersion;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class ProfileSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "profile_section_bullets", joinColumns = @JoinColumn(name = "profile_section_bullet_id"))
    private List<String> bullets;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_section_id"))
    private UserProfile profile;
}
