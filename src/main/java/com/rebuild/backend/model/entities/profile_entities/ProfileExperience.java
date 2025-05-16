package com.rebuild.backend.model.entities.profile_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.superclasses.SuperclassExperience;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "profile_experiences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_experiences", columnNames = {
                "company_name", "profile_id"
        })
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileExperience extends SuperclassExperience {

    public ProfileExperience(String companyName, List<String> technologyList, String location,
                      YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(companyName, technologyList, location, startDate, endDate, bullets);
    }

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_profile_experience_id"))
    @JsonIgnore
    private UserProfile profile;
}
