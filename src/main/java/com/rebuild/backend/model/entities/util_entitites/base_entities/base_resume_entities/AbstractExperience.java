package com.rebuild.backend.model.entities.util_entitites.base_entities.base_resume_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ExperienceBulletPoint;
import com.rebuild.backend.model.entities.util_entitites.base_entities.ProjectBulletPoint;
import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_experiences")
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractExperience extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    protected String companyName;

    @Column(name = "technology_list")
    protected String technologyList;

    @Column(name = "location", nullable = false)
    @NonNull
    protected String location;

    @Column(name = "experience_type")
    @NonNull
    protected String experienceType;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "end_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;

    @OneToMany(mappedBy = "associatedExperience", fetch = FetchType.LAZY, orphanRemoval = true,
            cascade = {
                    CascadeType.REMOVE,
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    protected List<ExperienceBulletPoint> bullets;

    public ExperienceResponse toResponse() {
        return new ExperienceResponse(this.id, this.companyName, this.technologyList,
                this.location, this.experienceType, StringUtil.transformYearMonth(this.startDate),
                StringUtil.transformYearMonth(this.endDate),
                bullets.stream().map(
                        AbstractBulletPoint::getText
                ).toList());
    }
}
