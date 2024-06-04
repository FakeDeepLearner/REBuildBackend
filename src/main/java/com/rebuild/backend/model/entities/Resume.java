package com.rebuild.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.mapping.Collection;

import java.util.ArrayList;
import java.util.Collections;
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

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER)
    @NonNull
    private Header header;

    @OneToOne(mappedBy = "resume", fetch = FetchType.EAGER)
    @NonNull
    private Education education;

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER, cascade = {
            CascadeType.ALL
    })
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
}
