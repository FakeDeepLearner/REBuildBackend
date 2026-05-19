package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostResumeProject;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractProject;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;

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


    public ResumeProject(String projectName, List<String> technologyList,
                         YearMonth startDate, YearMonth endDate, List<String> bullets)
    {
        super(projectName, technologyList, startDate, bullets);
        this.endDate = endDate;
    }

    public static ResumeProject copy(ResumeProject other)
    {
        return new ResumeProject(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets);
    }

    public static PostResumeProject copy(ResumeProject other, PostResume postResume)
    {
        return new PostResumeProject(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets, postResume);
    }

}
