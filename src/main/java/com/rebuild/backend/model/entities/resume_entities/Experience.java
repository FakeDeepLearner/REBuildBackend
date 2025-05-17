package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.model.entities.superclasses.SuperclassExperience;

import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@Entity
@Table(name = "experiences",
        uniqueConstraints = {@UniqueConstraint(name = "uk_resume_company",
                columnNames = {"company_name", "resume_id"})})
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Experience extends SuperclassExperience implements ResumeProperty{

    public Experience(String companyName, List<String> technologyList, String location,
                      YearMonth startDate, YearMonth endDate, List<String> bullets) {
        super(companyName, technologyList, location, startDate, endDate, bullets);
    }

    @ManyToOne(fetch =  FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "exp_fk_resume_id"))
    private Resume resume;

    public String toString() {
        return "\tEXPERIENCE:\n" +
                "\t\tCompany Name: " + companyName + "\n" +
                "\t\tTechnologies: " + technologyList + "\n" +
                "\t\tLocation: " + location + "\n" +
                "\t\tBullets: " + bullets + "\n" +
                "\t\tStart Date: " + startDate + "\n" +
                "\t\tEnd Date: " + endDate + "\n";
    }

}
