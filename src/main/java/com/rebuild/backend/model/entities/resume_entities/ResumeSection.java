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
public class ResumeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    @Convert(converter = DatabaseEncryptor.class)
    private String title;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true,
            mappedBy = "associatedSection")
    private List<ResumeSectionEntry> entries;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "section_fk_resume_id"))
    @JsonIgnore
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_version_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private ResumeVersion associatedVersion;

    public String toString() {
        return "\tSECTION:\n" +
                entries;
    }
}
