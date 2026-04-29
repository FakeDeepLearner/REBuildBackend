package com.rebuild.backend.model.entities.resume_entities.reviews_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "resume_snapshots")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@NoArgsConstructor
public class ResumeSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @OneToMany(mappedBy = "associatedSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSnapshotNode> nodes;

    @Column(name = "context")
    @NonNull
    private String context;

    @ManyToOne(cascade = {CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User associatedUser;
}
