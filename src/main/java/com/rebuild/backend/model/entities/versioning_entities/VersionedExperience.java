package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.superclasses.SuperclassExperience;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Table(name = "versioned_experiences")
@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor

public class VersionedExperience extends SuperclassExperience {

    public VersionedExperience(String companyName, List<String> technologyList, String location,
                      YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(companyName, technologyList, location, startDate, endDate, bullets);
    }

    @ManyToOne(fetch =  FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "version_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "exp_fk_version_id"))
    private ResumeVersion associatedVersion;
}
