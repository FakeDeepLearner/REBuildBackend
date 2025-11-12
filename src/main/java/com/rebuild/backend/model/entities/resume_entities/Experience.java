package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.utils.elastic_utils.ExperienceTypeBridge;
import com.rebuild.backend.utils.database_utils.GenerateV7UUID;
import com.rebuild.backend.utils.converters.database_converters.ExperienceTypesConverter;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import com.rebuild.backend.utils.serializers.ExperienceTypesSerializer;
import com.rebuild.backend.utils.serializers.YearMonthSerializer;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.search.mapper.pojo.bridge.mapping.annotation.ValueBridgeRef;
import org.hibernate.search.mapper.pojo.extractor.builtin.BuiltinContainerExtractors;
import org.hibernate.search.mapper.pojo.extractor.mapping.annotation.ContainerExtraction;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

import java.time.YearMonth;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Experience {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    @FullTextField
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(nullable = false)
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION))
    private List<String> technologyList;

    @Column(name = "location", nullable = false)
    @NonNull
    @FullTextField
    private String location;

    @Column(name = "types")
    @Convert(converter = ExperienceTypesConverter.class)
    @JsonSerialize(using = ExperienceTypesSerializer.class)
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION),
    valueBridge = @ValueBridgeRef(type = ExperienceTypeBridge.class))
    private Collection<ExperienceType> experienceTypes;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField
    private YearMonth startDate;

    @Column(name = "end_date")
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    @GenericField
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    @FullTextField(extraction = @ContainerExtraction(BuiltinContainerExtractors.COLLECTION))
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume = null;

    public String toString() {
        return "\tEXPERIENCE:\n" +
                "\t\tCompany Name: " + companyName + "\n" +
                "\t\tTechnologies: " + technologyList + "\n" +
                "\t\tLocation: " + location + "\n" +
                "\t\tType: " + experienceTypes + "\n" +
                "\t\tBullets: " + bullets + "\n" +
                "\t\tStart Date: " + startDate + "\n" +
                "\t\tEnd Date: " + endDate + "\n";
    }

    public static Experience copy(Experience other)
    {
        return new Experience(other.companyName, other.technologyList, other.location, other.experienceTypes,
                other.startDate, other.endDate, other.bullets);

    }

}
