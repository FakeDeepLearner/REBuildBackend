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

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header versionedHeader;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education versionedEducation;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<Experience> versionedExperiences;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<ResumeSection> versionedSections;

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
