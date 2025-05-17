package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.superclasses.SuperclassSectionEntry;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;


@Entity
@Table(name = "resume_section_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResumeSectionEntry extends SuperclassSectionEntry implements ResumeProperty {

    public ResumeSectionEntry(String title, List<String> toolsUsed, String location,
                              YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(title, toolsUsed, location, startDate, endDate, bullets);
    }

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_section_id", nullable = false, referencedColumnName = "id")
    @JsonIgnore
    private ResumeSection associatedSection;

    @Override
    public String toString() {
        return "\tSECTION_ENTRY:\n" +
                "\t\tTitle: " + title + "\n" +
                "\t\tTools: " + toolsUsed + "\n" +
                "\t\tLocation: " + location + "\n" +
                "\t\tBullets: " + bullets + "\n";
    }
}
