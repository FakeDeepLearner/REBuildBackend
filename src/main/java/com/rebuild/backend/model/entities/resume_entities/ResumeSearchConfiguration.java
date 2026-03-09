package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.user_entities.User;
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

    @Column(name = "creation_after")
    private Instant creationAfterCutoff;

    @Column(name = "creation_before")
    private Instant creationBeforeCutoff;

    @Column(name = "last_updated_at")
    private Instant lastUpdatedTime;

    @Column(name = "last_used_at")
    private Instant lastUsedTime;

    @ManyToOne(cascade = {
            CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @JsonIgnore
    private User user;


    public ResumeSearchConfiguration(ResumeSpecsForm baseForm)
    {
        this.resumeNameSearch = baseForm.resumeNameContains();
        this.creationAfterCutoff = Instant.parse(baseForm.creationAfterCutoff());
        this.creationBeforeCutoff = Instant.parse(baseForm.creationBeforeCutoff());
        this.lastUpdatedTime = Instant.now();
        this.lastUsedTime = null;
    }


}
