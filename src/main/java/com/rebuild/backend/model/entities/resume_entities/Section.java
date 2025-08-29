package com.rebuild.backend.model.entities.resume_entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Table(name = "resume_sections")
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class Section implements ResumeProperty{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JsonIgnore
    private UUID id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @NonNull
    private List<SectionEntry> entries;

    @NonNull
    private String title;


    public String toString() {
        return "\tSECTION:\n" +
                entries;
    }

    public static Section copy(Section other) {
        return new Section(other.entries.stream().
                map(SectionEntry::copy).toList(), other.title);
    }
}
