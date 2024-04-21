package com.rebuild.backend.model.entities;

import jakarta.persistence.*;
import lombok.*;

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

    @OneToMany(mappedBy = "resume", fetch = FetchType.EAGER, cascade = {
            CascadeType.ALL
    })
    @NonNull
    private List<Experience> experiences;

    @ManyToOne(cascade = {
            CascadeType.REFRESH
    })
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_user_id"))
    @NonNull
    private User user;
}
