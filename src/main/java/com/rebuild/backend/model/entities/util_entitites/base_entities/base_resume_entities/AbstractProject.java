package com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ProjectBulletPoint;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_projects")
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractProject extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @NonNull
    @Column(name = "project_name")
    protected String projectName;

    @Column(name = "technology_list")
    protected String technologyList;

    @NonNull
    @Column(name = "start_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "end_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;

    @OneToMany(mappedBy = "associatedProject", fetch = FetchType.LAZY, orphanRemoval = true,
            cascade = {
                    CascadeType.REMOVE,
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    protected List<ProjectBulletPoint> bullets;

    protected List<String> getBulletsDisplayValues()
    {
        return bullets.stream().map(
                AbstractBulletPoint::getText
        ).toList();
    }

    public ProjectResponse toResponse(){
        return new ProjectResponse(this.id, this.projectName, this.technologyList,
                StringUtil.getYearMonthDisplayValue(this.startDate), StringUtil.getYearMonthDisplayValue(this.endDate),
                getBulletsDisplayValues());
    }
}
