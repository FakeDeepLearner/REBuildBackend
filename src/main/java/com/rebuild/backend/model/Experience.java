package com.rebuild.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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


    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false, referencedColumnName = "id")
    private Resume resume;

}
