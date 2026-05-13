package com.rebuild.backend.model.entities.forum_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractHeader;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "post_resume_headers")
@Data
@NoArgsConstructor
public class PostResumeHeader extends AbstractHeader {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "post_resume_id", referencedColumnName = "id")
    @JsonIgnore
    private PostResume postResume;

    public PostResumeHeader(@NonNull String number,
                        @NonNull String name, @NonNull String email, @NonNull List<String> links,
                        PostResume postResume) {
        super(number, name, email, links);
        this.postResume = postResume;
    }
}
