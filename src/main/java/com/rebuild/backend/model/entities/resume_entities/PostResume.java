package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.utils.database_utils.GenerateV7UUID;
import com.rebuild.backend.utils.converters.database_converters.LocalDateTimeDatabaseConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "post_resumes", indexes = {
        @Index(columnList = "header_id"),
        @Index(columnList = "education_id"),
        @Index(columnList = "experience_id"),
        @Index(columnList = "user_id")
})
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
//@RequiredArgsConstructor
public class PostResume {

    @Id
    @GenerateV7UUID
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "header_id", referencedColumnName = "id")
    private Header header;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "education_id", referencedColumnName = "id")
    private Education education;

    @OneToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true)
    @OrderBy("endDate DESC NULLS FIRST, startDate DESC")
    @JoinColumn(name = "experience_id", referencedColumnName = "id")
    private List<Experience> experiences;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "post_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_post_id"))
    @JsonIgnore
    private ForumPost associatedPost;

    @JsonIgnore
    @Convert(converter = LocalDateTimeDatabaseConverter.class)
    private LocalDateTime creationTime = LocalDateTime.now();


    public PostResume(@NonNull Resume originalResume){
        Education originalEducation = originalResume.getEducation();
        Header originalHeader = originalResume.getHeader();
        List<Experience> originalExperiences = originalResume.getExperiences();
        // We are creating new objects here,
        // because we do not want them to be a reference to the original ones.
        this.education = Education.copy(originalEducation);
        this.header = Header.copy(originalHeader);
        this.experiences = originalExperiences.stream().map(
                Experience::copy).toList();
        this.creationTime = LocalDateTime.now();

    }
}
