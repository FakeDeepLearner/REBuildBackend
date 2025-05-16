package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.entities.superclasses.SuperclassEducation;


import jakarta.persistence.*;
import lombok.*;


import java.time.YearMonth;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "profile_educations")
public class ProfileEducation extends SuperclassEducation {

    public ProfileEducation(String schoolName, List<String> relevantCoursework,
                     String location, YearMonth startDate, YearMonth endDate) {
        super(schoolName, relevantCoursework, location, startDate, endDate);
    }

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_education_id"))
    @JsonIgnore
    private UserProfile profile;
}
