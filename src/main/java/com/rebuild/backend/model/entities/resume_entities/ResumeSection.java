package com.rebuild.backend.model.entities.resume_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
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
    })
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "section_fk_resume_id"))
    private Resume resume;
}
