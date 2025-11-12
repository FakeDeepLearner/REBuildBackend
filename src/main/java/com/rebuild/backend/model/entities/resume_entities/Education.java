package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.utils.database_utils.GenerateV7UUID;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import com.rebuild.backend.utils.serializers.YearMonthSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;


import java.time.YearMonth;
import java.util.List;
import java.util.UUID;



@Entity
@Table(name = "educations")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class Education {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    @GenericField
    private String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id"))
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION))
    private List<String> relevantCoursework;

    @Column(name = "location", nullable = false)
    @NonNull
    @FullTextField
    private String location;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField
    private YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField
    private YearMonth endDate;

    @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume = null;


    public String toString() {
        return "EDUCATION:\n" +
                "\tSchool Name: " + schoolName + "\n" +
                "\tCoursework: " + relevantCoursework + "\n" +
                "\tLocation: " + location +
                "\n\n\n";
    }

    public static Education copy(Education other)
    {
        return new Education(other.schoolName,
                other.relevantCoursework, other.location, other.startDate, other.endDate);
    }
}
