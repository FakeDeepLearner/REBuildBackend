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
@Table(name = "resume_sections")
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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private List<ResumeSectionEntry> entries;

    @NonNull
    private String title;


    public String toString() {
        return "\tSECTION:\n" +
                entries;
    }
}
