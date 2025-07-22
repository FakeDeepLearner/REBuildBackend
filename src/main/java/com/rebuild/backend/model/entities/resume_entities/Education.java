package com.rebuild.backend.model.entities.resume_entities;


import com.rebuild.backend.model.entities.superclasses.SuperclassEducation;

import jakarta.persistence.*;
import lombok.*;


import java.time.YearMonth;
import java.util.List;


@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "educations")
public class Education extends SuperclassEducation implements ResumeProperty{

    public Education(String schoolName, List<String> relevantCoursework,
                     String location, YearMonth startDate, YearMonth endDate) {
        super(schoolName, relevantCoursework, location, startDate, endDate);
    }

    public Education() {

    }


    public String toString() {
        return "EDUCATION:\n" +
                "\tSchool Name: " + schoolName + "\n" +
                "\tCoursework: " + relevantCoursework + "\n" +
                "\tLocation: " + location +
                "\n\n\n";
    }
}
