package com.rebuild.backend.model.entities.resume_entities.search_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.EmbeddedColumnNaming;

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

    @Embedded
    @EmbeddedColumnNaming()
    private HeaderSearchProperties headerSearchProperties;

    @Embedded
    @EmbeddedColumnNaming()
    private EducationSearchProperties educationSearchProperties;

    @Embedded
    @EmbeddedColumnNaming()
    private ExperienceSearchProperties experienceSearchProperties;

    @Embedded
    @EmbeddedColumnNaming()
    private ProjectSearchProperties projectSearchProperties;

    @Column(name = "creation_after")
    private Instant creationAfterCutoff;

    @Column(name = "creation_before")
    private Instant creationBeforeCutoff;

    @ManyToOne(cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;


    public ResumeSearchConfiguration(ResumeSpecsForm baseForm)
    {
        this.headerSearchProperties = new HeaderSearchProperties(baseForm.firstNameContains(),
                baseForm.lastNameContains());
        this.educationSearchProperties = new EducationSearchProperties(baseForm.schoolNameContains(),
                baseForm.courseWorkContains());
        this.experienceSearchProperties = new ExperienceSearchProperties(baseForm.companyContains(), baseForm.experienceBulletsContains(),
                baseForm.experienceTechnologyListContains());
        this.projectSearchProperties = new ProjectSearchProperties(baseForm.projectNameContains(),
                baseForm.projectTechnologyListContains(), baseForm.projectBulletsContains());
        this.resumeNameSearch = baseForm.resumeNameContains();
        this.creationAfterCutoff = Instant.parse(baseForm.creationAfterCutoff());
        this.creationBeforeCutoff = Instant.parse(baseForm.creationBeforeCutoff());
    }


}
