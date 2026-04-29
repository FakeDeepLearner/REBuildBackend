package com.rebuild.backend.model.entities.resume_entities.reviews_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "napshot_nodes")
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResumeSnapshotNode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    //This "path" is the materialized path of the node in its snapshot.
    @NonNull
    private String path;

    @NonNull
    private String content;

    @ManyToOne(cascade = {
            CascadeType.REFRESH,
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false, referencedColumnName = "id")
    private ResumeSnapshot associatedSnapshot;

    @OneToMany(mappedBy = "snapshotNode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeReview> reviews;

}
