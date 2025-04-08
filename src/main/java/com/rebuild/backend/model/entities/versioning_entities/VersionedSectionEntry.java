package com.rebuild.backend.model.entities.versioning_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Table(name = "versioned_section_entries")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class VersionedSectionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(nullable = false, name = "title")
    @NonNull
    private String title;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "versioned_section_tools",
            joinColumns = @JoinColumn(name = "versioned_section_entry_id"))
    private List<String> toolsUsed;

    @Column(nullable = false, name = "location")
    @NonNull
    private String location;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "versioned_section_bullets",
            joinColumns = @JoinColumn(name = "versioned_section_entry_id"))
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private VersionedSection associatedSection;
}
