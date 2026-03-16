package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
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
@Table(name = "educations")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Education implements Serializable {

    @Serial
    private static final long serialVersionUID = 3L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id"))
    private List<String> relevantCoursework;

    @Column(name = "location", nullable = false)
    @NonNull
    private String location;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion version;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

    public Education(String schoolName, List<String> relevantCoursework, String location,
                     YearMonth startDate, YearMonth endDate) {
        this.schoolName = schoolName;
        this.relevantCoursework = relevantCoursework;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Education copy(Education other)
    {
        return new Education(other.schoolName,
                other.relevantCoursework, other.location, other.startDate, other.endDate);
    }

    public static Education sensitiveCopy(Education other)
    {
        return new Education(StringUtil.maskString(other.schoolName),
                other.relevantCoursework, other.location, other.startDate, other.endDate);
    }
}
