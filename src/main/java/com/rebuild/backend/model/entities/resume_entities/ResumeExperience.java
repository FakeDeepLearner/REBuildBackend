package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostResumeExperience;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractExperience;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "resume_experiences")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeExperience extends AbstractExperience {

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

    public ResumeExperience(String companyName, List<String> technologyList, String location,
                            String experienceType, YearMonth startDate, YearMonth endDate,
                            List<String> bullets) {
        super(companyName, location, experienceType, startDate, bullets);
        this.endDate = endDate;
        this.technologyList = technologyList;
    }


    public static ResumeExperience copy(ResumeExperience other)
    {
        return new ResumeExperience(other.companyName, other.technologyList, other.location, other.experienceType,
                other.startDate, other.endDate, other.bullets);

    }

    public static PostResumeExperience copy(ResumeExperience other, PostResume postResume)
    {
        return new PostResumeExperience(other.companyName,
                other.technologyList, other.location, other.experienceType,
                other.startDate, other.endDate, other.bullets, postResume);

    }
}
