package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractEducation;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

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

    public PostResumeEducation(String schoolName, String relevantCoursework, String location,
                           YearMonth startDate, YearMonth endDate,
                               PostResume postResume) {
        super(schoolName, relevantCoursework, location, startDate);
        this.endDate = endDate;
        this.postResume = postResume;
    }

    @Override
    public EducationResponse toResponse() {
        return new EducationResponse(StringUtil.maskString(this.schoolName),
                this.relevantCoursework, this.location,
                StringUtil.transformYearMonth(this.startDate), StringUtil.transformYearMonth(this.endDate));
    }
}
