package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rebuild.backend.utils.converters.encrypt.DatabaseEncryptor;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name = "resume_sections", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resume_section", columnNames = {"resume_id", "title"})
})
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class ResumeSection implements ResumeProperty{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true,
    mappedBy = "associatedSection")
    private List<ResumeSectionEntry> entries;

    @NonNull
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "section_fk_resume_id"))
    @JsonIgnore
    private Resume resume;


    public String toString() {
        return "\tSECTION:\n" +
                entries;
    }
}
