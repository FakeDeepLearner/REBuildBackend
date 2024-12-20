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

    @OneToOne(mappedBy = "associatedVersion", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @NonNull
    private Header versionedHeader;

    @OneToOne(mappedBy = "associatedVersion", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @NonNull
    private Education versionedEducation;

    /*
    * Unlike most one-to-many relations,
    * we do not want to delete the experiences or sections when we delete this version,
    * because those objects are also being used in resumes as well.
    */
    @OneToMany(mappedBy = "associatedVersion", fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @NonNull
    private List<Experience> versionedExperiences;


    @OneToMany(mappedBy = "associatedVersion", fetch = FetchType.EAGER,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST})
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
