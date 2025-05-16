package com.rebuild.backend.model.entities.versioning_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.superclasses.SuperclassSectionEntry;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Table(name = "versioned_section_entries")
@Entity
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class VersionedSectionEntry extends SuperclassSectionEntry {

    public VersionedSectionEntry(String title, List<String> toolsUsed, String location,
                              YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(title, toolsUsed, location, startDate, endDate, bullets);
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private VersionedSection associatedSection;
}
