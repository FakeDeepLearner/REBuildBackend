package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "resume_search_configs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResumeSearchConfiguration {

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
    private String bulletsSearch;

    @Column(name = "technologies_search")
    private String technologiesSearch;

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
        this.bulletsSearch = baseForm.bulletsContains();
        this.resumeNameSearch = baseForm.resumeNameContains();
        this.firstNameSearch = baseForm.firstNameContains();
        this.lastNameSearch = baseForm.lastNameContains();
        this.schoolNameSearch = baseForm.schoolNameContains();
        this.courseworkSearch = baseForm.courseWorkContains();
        this.companySearch = baseForm.companyContains();
        this.technologiesSearch = baseForm.technologyListContains();
        this.creationAfterCutoff = Instant.parse(baseForm.creationAfterCutoff());
        this.creationBeforeCutoff = Instant.parse(baseForm.creationBeforeCutoff());
    }


}
