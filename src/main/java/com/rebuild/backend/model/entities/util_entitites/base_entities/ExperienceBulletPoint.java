package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractExperience;
import com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities.AbstractProject;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experience_bullets")
@Data
@NoArgsConstructor
public class ExperienceBulletPoint extends AbstractBulletPoint {

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinColumn(name = "experience_id", nullable = false, referencedColumnName = "id")
    protected AbstractExperience associatedExperience;


    public ExperienceBulletPoint(String text, AbstractExperience associatedExperience)
    {
        super(text);
        this.associatedExperience = associatedExperience;
    }
}
