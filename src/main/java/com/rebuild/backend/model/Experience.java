package com.rebuild.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "experiences")
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class Experience {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id"
    )
    private UUID id;


    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id",
        foreignKey = @ForeignKey(name = "fk_resume_id"))
    @NonNull
    private Resume resume;

}
