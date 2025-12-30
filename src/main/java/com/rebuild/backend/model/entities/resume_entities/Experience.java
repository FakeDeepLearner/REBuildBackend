package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import com.rebuild.backend.utils.converters.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.converters.DatabaseEncryptor;
import com.rebuild.backend.utils.serializers.YearMonthSerializer;
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
@Table(name = "experiences")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Experience implements Serializable {

    @Serial
    private static final long serialVersionUID = 4L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    @FullTextField(searchable = Searchable.YES)
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "experience_technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION),
    searchable = Searchable.YES)
    private List<String> technologyList;

    @Column(name = "location", nullable = false)
    @NonNull
    @FullTextField(searchable = Searchable.YES)
    private String location;

    @Column(name = "experience_type")
    @NonNull
    @FullTextField(searchable =  Searchable.YES)
    private String experienceType;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField(searchable = Searchable.YES, sortable =  Sortable.YES)
    private YearMonth startDate;

    @Column(name = "end_date")
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField(searchable = Searchable.YES, sortable = Sortable.YES)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "experience_bullets", joinColumns = @JoinColumn(name = "experience_id"))
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

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    @JsonIgnore
    private UserProfile profile;

    public Experience(String companyName, List<String> technologyList, String location,
                      String experienceType, YearMonth startDate, YearMonth endDate,
                      List<String> bullets) {
        this.companyName = companyName;
        this.technologyList = technologyList;
        this.location = location;
        this.experienceType = experienceType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.bullets = bullets;

    }

    public static Experience copy(Experience other)
    {
        return new Experience(other.companyName, other.technologyList, other.location, other.experienceType,
                other.startDate, other.endDate, other.bullets);

    }

}
