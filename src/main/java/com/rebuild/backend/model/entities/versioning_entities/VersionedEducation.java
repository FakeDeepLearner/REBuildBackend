package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.superclasses.SuperclassEducation;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Table(name = "versioned_educations")
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class VersionedEducation extends SuperclassEducation {

    public VersionedEducation(String schoolName, List<String> relevantCoursework,
                     String location, YearMonth startDate, YearMonth endDate) {
        super(schoolName, relevantCoursework, location, startDate, endDate);
    }
    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "version_id", referencedColumnName = "id", nullable = false,
            foreignKey = @ForeignKey(name = "ed_fk_version_id"))
    private ResumeVersion associatedVersion;
}
