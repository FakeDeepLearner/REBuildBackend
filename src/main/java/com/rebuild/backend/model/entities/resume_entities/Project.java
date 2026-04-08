package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.responses.resume_responses.ProjectResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Project{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(name = "project_name")
    private String projectName;

    @ElementCollection
    @CollectionTable(name = "project_technologies",
            joinColumns = @JoinColumn(name = "project_id", referencedColumnName = "id"))
    @NonNull
    private List<String> technologyList;

    @NonNull
    @Column(name = "start_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "project_bullets", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;


    public Project(String projectName, List<String> technologyList,
                   YearMonth startDate, YearMonth endDate, List<String> bullets)
    {
        this.projectName = projectName;
        this.technologyList = technologyList;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bullets = bullets;
    }

    public static Project copy(Project other)
    {
        return new Project(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets);
    }

    public static Project sensitiveCopy(Project other)
    {
        return new Project(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets);
    }

    public ProjectResponse toResponse()
    {
        return new ProjectResponse(this.id, this.projectName, this.technologyList,
                StringUtil.transformYearMonth(this.startDate), StringUtil.transformYearMonth(this.endDate),
                this.bullets);
    }


}
