package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "educations")
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @NonNull
    private String schoolName;

    @NonNull
    @ElementCollection
    @CollectionTable(name = "courses", joinColumns = @JoinColumn(name = "education_id"))
    private List<String> relevantCoursework;

    @OneToOne(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "resume_id", referencedColumnName = "id",
    foreignKey = @ForeignKey(name = "ed_fk_resume_id"))
    private Resume resume;

    @OneToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE,
            CascadeType.PERSIST,
    })
    @JoinColumn(name = "associated_version_id", referencedColumnName = "id")
    @JsonIgnore
    private ResumeVersion associatedVersion;

    public String toString() {
        return "EDUCATION:\n" +
                "\tSchool Name: " + schoolName + "\n" +
                "\tCoursework: " + relevantCoursework + "\n\n\n";
    }
}
