package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@NamedQuery(
        name = "ResumeVersion.findAllByResumeIdWithLimit",
        query = "SELECT v FROM ResumeVersion v WHERE v.associatedResume.id=:resumeId"
)
@Entity
@Table(name = "versions", indexes = {
        @Index(columnList = "id, created_date"),
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "versioned_name", nullable = false)
    private String versionedName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header versionedHeader;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education versionedEducation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "experience_id", referencedColumnName = "id")
    private List<Experience> versionedExperiences;

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime createdDate = LocalDateTime.now();

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "associated_resume_id", nullable = false, referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "fk_version_resume_id"))
    private Resume associatedResume;

    public static ResumeVersion copy(ResumeVersion resumeVersion) {
        return new ResumeVersion(resumeVersion);
    }

    public ResumeVersion(ResumeVersion other)
    {
        this.associatedResume = other.associatedResume;
        this.versionedHeader = Header.copy(other.versionedHeader);
        this.versionedEducation = Education.copy(other.versionedEducation);
        this.versionedExperiences = other.versionedExperiences.stream()
                .map(Experience::copy).toList();
        this.versionedName = other.versionedName;
        this.createdDate = LocalDateTime.now();
    }
}
