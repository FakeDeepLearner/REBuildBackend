package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profile_sections", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_sections", columnNames = {
                "title", "profile_id"
        })
})
public class ProfileSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    private String title;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "associatedSection"
            , orphanRemoval = true)
    private List<ProfileSectionEntry> entries;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_section_id"))
    @JsonIgnore
    private UserProfile profile;
}
