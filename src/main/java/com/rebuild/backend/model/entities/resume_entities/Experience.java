package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.rebuild.backend.utils.converters.database_converters.ExperienceTypesConverter;
import com.rebuild.backend.utils.converters.database_converters.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.converters.database_converters.DatabaseEncryptor;
import com.rebuild.backend.utils.serializers.ExperienceTypesSerializer;
import com.rebuild.backend.utils.serializers.YearMonthSerializer;
import jakarta.persistence.*;
import lombok.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Experience implements ResumeProperty {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(nullable = false)
    @NonNull
    private List<String> technologyList;

    @Column(name = "location", nullable = false)
    @NonNull
    private String location;

    @Column(name = "types")
    @Convert(converter = ExperienceTypesConverter.class)
    @JsonSerialize(using = ExperienceTypesSerializer.class)
    @NonNull
    private Collection<ExperienceType> experienceTypes;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date", nullable = false)
    @NonNull
    @JsonSerialize(using = YearMonthSerializer.class)
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;


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
