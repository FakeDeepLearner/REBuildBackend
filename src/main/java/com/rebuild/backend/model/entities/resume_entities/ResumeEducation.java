package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostResumeEducation;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractEducation;
import com.rebuild.backend.model.responses.resume_responses.EducationResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;


import java.time.YearMonth;
import java.util.List;
import java.util.UUID;



@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "resume_educations")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeEducation extends AbstractEducation {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

    public ResumeEducation(String schoolName, List<String> relevantCoursework, String location,
                           YearMonth startDate, YearMonth endDate) {
        super(schoolName, relevantCoursework, location, startDate);
        this.setEndDate(endDate);
    }

    public static ResumeEducation copy(ResumeEducation other)
    {
        return new ResumeEducation(other.schoolName, other.relevantCoursework,
                other.location, other.startDate, other.endDate);
    }

    public static PostResumeEducation sensitiveCopy(ResumeEducation other, PostResume postResume)
    {
        return new PostResumeEducation(StringUtil.maskString(other.getSchoolName()),
                other.getRelevantCoursework(), other.getLocation(), other.getStartDate(),
                other.getEndDate(), postResume);
    }

}
