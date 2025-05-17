package com.rebuild.backend.model.entities.resume_entities;


import com.rebuild.backend.model.entities.superclasses.SuperclassEducation;

import jakarta.persistence.*;
import lombok.*;


import java.time.YearMonth;
import java.util.List;


@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "educations")
public class Education extends SuperclassEducation implements ResumeProperty{

    public Education(String schoolName, List<String> relevantCoursework,
                     String location, YearMonth startDate, YearMonth endDate) {
        super(schoolName, relevantCoursework, location, startDate, endDate);
    }

    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "ed_fk_resume_id"))
    private Resume resume;

    public String toString() {
        return "EDUCATION:\n" +
                "\tSchool Name: " + schoolName + "\n" +
                "\tCoursework: " + relevantCoursework + "\n" +
                "\tLocation: " + location +
                "\n\n\n";
    }
}
