package com.rebuild.backend.model.entities.util_entitites.base_entities;

import com.rebuild.backend.model.entities.util_entitites.Auditable;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@MappedSuperclass
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

    @ElementCollection
    @CollectionTable(name = "project_technologies",
            joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"))
    @NonNull
    protected List<String> technologyList;

    @NonNull
    @Column(name = "start_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth startDate;

    @Column(name = "end_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    protected YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "project_bullets", joinColumns = @JoinColumn(name = "project_id",
            referencedColumnName = "id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    protected List<String> bullets;

    public ProjectResponse toResponse(){
        return new ProjectResponse(this.id, this.projectName, this.technologyList,
                StringUtil.transformYearMonth(this.startDate), StringUtil.transformYearMonth(this.endDate),
                this.bullets);
    }
}
