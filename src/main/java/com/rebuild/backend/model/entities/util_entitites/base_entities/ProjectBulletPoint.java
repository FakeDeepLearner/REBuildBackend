package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractProject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "project_bullets")
@Data
@NoArgsConstructor
public class ProjectBulletPoint extends AbstractBulletPoint {

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, referencedColumnName = "id")
    protected AbstractProject associatedProject;


    public ProjectBulletPoint(String text, AbstractProject associatedProject)
    {
        super(text);
        this.associatedProject = associatedProject;
    }
}
