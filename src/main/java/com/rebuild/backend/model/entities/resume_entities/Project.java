package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
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
    @FullTextField(searchable = Searchable.YES)
    private String projectName;

    @ElementCollection
    @CollectionTable(name = "project_technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION),
            searchable = Searchable.YES)
    private List<String> technologyList;

    @NonNull
    @Column(name = "start_date")
    @GenericField(sortable = Sortable.YES, searchable = Searchable.YES)
    private YearMonth startDate;

    @NonNull
    @Column(name = "end_date")
    @GenericField(sortable = Sortable.YES, searchable = Searchable.YES)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "project_bullets", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION),
            searchable = Searchable.YES)
    private List<String> bullets;


    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion version;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    @JsonIgnore
    private UserProfile profile;

    public static Project copy(Project other)
    {
        return new Project(other.projectName, other.technologyList,
                other.startDate, other.endDate, other.bullets);
    }


}
