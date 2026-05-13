package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractExperience;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_resume_experiences")
@Data
@NoArgsConstructor
public class PostResumeExperience extends AbstractExperience {

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_resume_id", referencedColumnName = "id")
    @JsonIgnore
    private PostResume postResume;

    public PostResumeExperience(String companyName, List<String> technologyList, String location,
                            String experienceType, YearMonth startDate, YearMonth endDate,
                            List<String> bullets, PostResume postResume) {
        super(companyName, technologyList, location, experienceType, startDate, bullets);
        this.endDate = endDate;
        this.postResume = postResume;
    }
}
