package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "versions")
@Data
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @Column(name = "versioned_name", nullable = false)
    @NonNull
    private String versionedName;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id")
    @MapsId
    @NonNull
    private Header versionedHeader;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinColumn(name = "education_id")
    @MapsId
    @NonNull
    private Education versionedEducation;

    /*
    * Unlike most one-to-many relations,
    * we do not want to delete the experiences or sections when we delete this version,
    * because those objects are also being used in resumes as well.
    */
    @OneToMany
    @JoinTable(
            name = "versions_and_experiences",
            joinColumns = @JoinColumn(name = "version_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "experience_id", referencedColumnName = "id")
    )
    @NonNull
    private List<Experience> versionedExperiences;


    @OneToMany
    @JoinTable(
            name = "versions_and_section",
            joinColumns = @JoinColumn(name = "version_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "section_id", referencedColumnName = "id")
    )
    @NonNull
    private List<ResumeSection> versionedSections;

    @ManyToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "associated_resume_id", nullable = false, referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "fk_version_resume_id"))
    private Resume associatedResume;
}
