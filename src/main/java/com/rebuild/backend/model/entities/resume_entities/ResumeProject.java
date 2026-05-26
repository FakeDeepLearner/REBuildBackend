package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostResumeProject;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ProjectBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractProject;
import com.rebuild.backend.utils.BulletsUtil;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "resume_projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeProject extends AbstractProject {

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;


    public ResumeProject(String projectName, String technologyList,
                         YearMonth startDate, YearMonth endDate, List<String> bullets)
    {
        super(projectName, startDate);
        this.endDate = endDate;
        this.technologyList = technologyList;
        this.bullets = BulletsUtil.createProjectBullets(bullets, this);
    }

    public ResumeProject(ResumeProject other)
    {
        super(other.projectName, other.startDate);
        this.endDate = other.endDate;
        this.technologyList = other.technologyList;
        this.bullets = BulletsUtil.copyProjectBullets(other.bullets, this);

    }

    public static PostResumeProject copy(ResumeProject other, PostResume postResume)
    {
        return new PostResumeProject(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets, postResume);
    }

}
