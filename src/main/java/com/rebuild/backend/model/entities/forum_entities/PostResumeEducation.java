package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractEducation;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_resume_educations")
@Data
@NoArgsConstructor
public class PostResumeEducation extends AbstractEducation {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_resume_id", referencedColumnName = "id")
    @JsonIgnore
    private PostResume postResume;

    public PostResumeEducation(String schoolName, List<String> relevantCoursework, String location,
                           YearMonth startDate, YearMonth endDate,
                               PostResume postResume) {
        super(schoolName, relevantCoursework, location, startDate);
        this.endDate = endDate;
        this.postResume = postResume;
    }
}
