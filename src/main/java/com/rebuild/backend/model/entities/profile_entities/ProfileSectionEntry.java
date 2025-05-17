package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.superclasses.SuperclassSectionEntry;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "profile_section_entries")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class ProfileSectionEntry extends SuperclassSectionEntry {

    public ProfileSectionEntry(String title, List<String> toolsUsed, String location,
                              YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(title, toolsUsed, location, startDate, endDate, bullets);
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private ProfileSection associatedSection;


}
