package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_search_configs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeSearchConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = 9L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "resume_name_search")
    private String resumeNameSearch;

    @Column(name = "first_name_search")
    private String firstNameSearch;

    @Column(name = "last_name_search")
    private String lastNameSearch;

    @Column(name = "school_name_search")
    private String schoolNameSearch;

    @Column(name = "coursework_search")
    private String courseworkSearch;

    @Column(name = "company_search")
    private String companySearch;

    @Column(name = "bullets_search")
    private String experienceBulletsSearch;

    @Column(name = "technologies_search")
    private String experienceTechnologiesSearch;

    @Column(name = "project_name_search")
    private String projectNameSearch;

    @Column(name = "project_technologies_search")
    private String projectTechnologyListSearch;

    @Column(name = "project_bullets_search")
    private String projectBulletsSearch;

    @Column(name = "creation_after")
    private Instant creationAfterCutoff;

    @Column(name = "creation_before")
    private Instant creationBeforeCutoff;

    @ManyToOne(cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "profile_id", referencedColumnName = "id")
    @JsonIgnore
    private UserProfile associatedProfile;


    public ResumeSearchConfiguration(ResumeSpecsForm baseForm)
    {
        this.experienceBulletsSearch = baseForm.experienceBulletsContains();
        this.resumeNameSearch = baseForm.resumeNameContains();
        this.firstNameSearch = baseForm.firstNameContains();
        this.lastNameSearch = baseForm.lastNameContains();
        this.schoolNameSearch = baseForm.schoolNameContains();
        this.courseworkSearch = baseForm.courseWorkContains();
        this.companySearch = baseForm.companyContains();
        this.experienceTechnologiesSearch = baseForm.experienceTechnologyListContains();
        this.projectNameSearch = baseForm.projectNameContains();
        this.projectTechnologyListSearch = baseForm.projectTechnologyListContains();
        this.projectBulletsSearch = baseForm.projectBulletsContains();
        this.creationAfterCutoff = Instant.parse(baseForm.creationAfterCutoff());
        this.creationBeforeCutoff = Instant.parse(baseForm.creationBeforeCutoff());
    }


}
