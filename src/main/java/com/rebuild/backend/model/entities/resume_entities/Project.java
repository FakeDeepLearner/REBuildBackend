package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.database_utils.YearMonthSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.engine.backend.types.Searchable;
import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.io.Serial;
import java.io.Serializable;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 11L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    @Column(name = "project_name")
    private String projectName;

    @ElementCollection
    @CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @NonNull
    private List<String> technologyList;

    @NonNull
    @Column(name = "start_date")
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date")
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "project_bullets", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion version;

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


}
