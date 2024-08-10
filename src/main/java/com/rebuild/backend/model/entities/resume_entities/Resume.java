package com.rebuild.backend.model.entities.resume_entities;

import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Experience;
import com.rebuild.backend.model.entities.resume_entities.Header;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resumes")
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

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Header header;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Education education;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER, cascade = {
            CascadeType.ALL
    }, orphanRemoval = true)
    @NonNull
    private List<Experience> experiences;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    @NonNull
    private User user;

    public Resume(@NonNull User user){
        this.user = user;
        this.experiences = new ArrayList<>();
        this.education = new Education();
        this.header = new Header();
    }

    public void addExperience(Experience experience){
        experiences.add(experience);
    }
}
