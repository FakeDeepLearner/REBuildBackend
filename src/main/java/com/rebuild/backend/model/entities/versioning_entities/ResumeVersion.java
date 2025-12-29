package com.rebuild.backend.model.entities.versioning_entities;

import com.rebuild.backend.model.entities.resume_entities.*;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
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
public class ResumeVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 7L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "versioned_name", nullable = false)
    private String versionedName;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "version")
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header versionedHeader;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "version")
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education versionedEducation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "version")
    @JoinColumn(name = "experience_id", referencedColumnName = "id")
    private List<Experience> versionedExperiences;

    @Column(name = "created_date", nullable = false, updatable = false)
    @CreatedDate
    private Instant createdDate = Instant.now();

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
        this.createdDate = Instant.now();
    }
}
