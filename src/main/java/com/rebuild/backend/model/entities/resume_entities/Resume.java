package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.model.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_same_user_resume_name", columnNames = {"user_id", "name"})
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;

    @Column(name = "name", nullable = false)
    @NonNull
    private String name;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Header header;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Education education;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true)
    private List<Experience> experiences;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSection> sections;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    private User user;

    public Resume(@NonNull String resume_name, @NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
        this.sections = null;
        this.name = resume_name;
    }

    public void addExperience(Experience experience){
        experiences.add(experience);
    }

    public void addSection(ResumeSection section){
        if (sections == null){
            sections = new ArrayList<>();
            sections.add(section);
        }
        else{
            sections.add(section);
        }
    }
}
