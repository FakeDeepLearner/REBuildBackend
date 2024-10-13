package com.rebuild.backend.model.entities.resume_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class ResumeSection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NonNull
    private String title;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "section_bullets", joinColumns = @JoinColumn(name = "section_bullet_id"))
    private List<String> bullets;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "section_fk_resume_id"))
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "associated_version_id", referencedColumnName = "id", nullable = false)
    private ResumeVersion associatedVersion;

    public String toString() {
        return "\tSECTION:\n" +
                "\t\tTitle: " + title + "\n" +
                "\t\tBullets: " + bullets + "\n";
    }
}
