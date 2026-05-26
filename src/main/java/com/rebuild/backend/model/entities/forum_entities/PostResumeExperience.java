package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ExperienceBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractExperience;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.StringUtil;
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

    public PostResumeExperience(String companyName, String technologyList, String location,
                            String experienceType, YearMonth startDate, YearMonth endDate,
                            List<ExperienceBulletPoint> bullets, PostResume postResume) {
        super(companyName, location, experienceType, startDate);
        List<ExperienceBulletPoint> bulletPoints = BulletsUtil.copyExperienceBullets(bullets, this);
        this.endDate = endDate;
        this.postResume = postResume;
        this.technologyList = technologyList;
        this.bullets = bulletPoints;
    }

    @Override
    public ExperienceResponse toResponse() {
        return new ExperienceResponse(null, StringUtil.maskString(this.companyName),
                this.technologyList, this.location, this.experienceType,
                StringUtil.getYearMonthDisplayValue(this.startDate), StringUtil.getYearMonthDisplayValue(this.endDate),
                getBulletsDisplayValues());
    }
}
