package com.rebuild.backend.model.entities.resume_entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "template_metadata")
public class TemplateMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "template_id")
    private UUID templateID;

    @Column(name = "template_name", nullable = false)
    @NonNull
    private String templateName;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "resume_id", referencedColumnName = "id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_template_resume_id"))
    @NonNull
    private Resume resume;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();
}
