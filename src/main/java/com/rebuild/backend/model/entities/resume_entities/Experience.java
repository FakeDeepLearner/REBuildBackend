package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.responses.resume_responses.ExperienceResponse;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.YearMonthDatabaseConverter;
import com.rebuild.backend.utils.database_utils.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class Experience{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "experience_technologies",
            joinColumns = @JoinColumn(name = "experience_id", referencedColumnName = "id"))
    @NonNull
    private List<String> technologyList;

    @Column(name = "location", nullable = false)
    @NonNull
    private String location;

    @Column(name = "experience_type")
    @NonNull
    private String experienceType;

    @Column(name = "start_date", nullable = false)
    @NonNull
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth startDate;

    @Column(name = "end_date")
    @Convert(converter = YearMonthDatabaseConverter.class)
    private YearMonth endDate;

    @ElementCollection
    @CollectionTable(name = "experience_bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

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

    public static Experience sensitiveCopy(Experience other)
    {
        return new Experience(StringUtil.maskString(other.companyName), other.technologyList, other.location, other.experienceType,
                other.startDate, other.endDate, other.bullets);
    }


    public ExperienceResponse toResponse()
    {
        return new ExperienceResponse(this.id, this.companyName, this.technologyList,
                this.location, this.experienceType, StringUtil.transformYearMonth(this.startDate),
                StringUtil.transformYearMonth(this.endDate), this.bullets);
    }

}
