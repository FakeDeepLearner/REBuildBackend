package com.rebuild.backend.model.entities.versioning_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
        @Index(columnList = "id, created_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(name = "versioned_name", nullable = false)

    private String versionedName;

    @OneToOne(mappedBy = "associatedVersion", cascade = CascadeType.ALL)
    private VersionedHeader versionedHeader;

    @OneToOne(mappedBy = "associatedVersion", cascade = CascadeType.ALL)
    private VersionedEducation versionedEducation;

    @OneToMany(mappedBy = "associatedVersion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VersionedExperience> versionedExperiences;

    @OneToMany(mappedBy = "associatedVersion", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<VersionedSection> versionedSections;

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
}
