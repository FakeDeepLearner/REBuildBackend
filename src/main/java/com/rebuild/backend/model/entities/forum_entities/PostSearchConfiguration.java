package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "post_search_configs")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostSearchConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = 10L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(name = "post_title_search")
    private String titleSearch;

    @Column(name = "post_body_search")
    private String bodySearch;

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

    public PostSearchConfiguration(ForumSpecsForm specsForm)
    {
        this.titleSearch = specsForm.titleContains();
        this.bodySearch = specsForm.bodyContains();
        this.creationAfterCutoff = Instant.parse(specsForm.postAfterCutoff());
        this.creationBeforeCutoff = Instant.parse(specsForm.postBeforeCutoff());
    }
}
