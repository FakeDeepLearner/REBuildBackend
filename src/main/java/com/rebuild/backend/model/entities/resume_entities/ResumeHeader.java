package com.rebuild.backend.model.entities.resume_entities;


import com.fasterxml.jackson.annotation.JsonIgnore;

import com.rebuild.backend.model.entities.forum_entities.PostResume;
import com.rebuild.backend.model.entities.forum_entities.PostResumeHeader;
import com.rebuild.backend.model.entities.util_entitites.base_entities.AbstractHeader;
import com.rebuild.backend.model.responses.resume_responses.HeaderResponse;
import com.rebuild.backend.utils.StringUtil;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "resume_headers")
@Data
@NoArgsConstructor
public class ResumeHeader extends AbstractHeader {

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name = "resume_id", referencedColumnName = "id")
    @JsonIgnore
    private Resume resume;

    public ResumeHeader(@NonNull String number,
                        @NonNull String name, @NonNull String email, @NonNull List<String> links) {
        super(number, name, email, links);
    }

    public static ResumeHeader copy(ResumeHeader other)
    {
        return new ResumeHeader(other.number, other.name, other.email, other.links);
    }

    public static PostResumeHeader copy(ResumeHeader other, PostResume postResume)
    {
        return new PostResumeHeader(other.number, other.name,
            other.email, other.links, postResume);
    }


}
