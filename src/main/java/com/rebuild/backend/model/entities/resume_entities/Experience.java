package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.utils.converters.DurationToStringConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiences",
        uniqueConstraints = {@UniqueConstraint(name = "uk_resume_company",
                columnNames = {"company_name", "resume_id"})})
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "company_name", nullable = false)
    @NonNull
    private String companyName;

    @ElementCollection
    @CollectionTable(name = "technologies", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(nullable = false)
    @NonNull
    private List<String> technologyList;

    @Column(name = "time_period", nullable = false)
    @NonNull
    @Convert(converter = DurationToStringConverter.class)
    private Duration timePeriod;

    @ElementCollection
    @CollectionTable(name = "bullets", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "bullets", nullable = false)
    @NonNull
    private List<String> bullets;

    @ManyToOne(fetch =  FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "exp_fk_resume_id"))
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_version_id", referencedColumnName = "id", nullable = false)
    private ResumeVersion associatedVersion;

    public String toString() {
        return "\tEXPERIENCE:\n" +
                "\t\tCompany Name: " + companyName + "\n" +
                "\t\tTechnologies: " + technologyList + "\n" +
                "\t\tBullets: " + bullets + "\n" +
                "\t\tTime Period: " + timePeriod + "\n";
    }

}
