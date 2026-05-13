package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractProject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.YearMonth;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_resume_projects")
@Data
@NoArgsConstructor
public class PostResumeProject extends AbstractProject {

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_resume_id", referencedColumnName = "id")
    @JsonIgnore
    private PostResume postResume;

    public PostResumeProject(String projectName, List<String> technologyList,
                         YearMonth startDate, YearMonth endDate, List<String> bullets,
                             PostResume postResume)
    {
        super(projectName, technologyList, startDate, bullets);
        this.endDate = endDate;
        this.postResume = postResume;
    }



}
