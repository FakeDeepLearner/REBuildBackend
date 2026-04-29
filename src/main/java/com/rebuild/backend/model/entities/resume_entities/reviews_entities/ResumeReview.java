package com.rebuild.backend.model.entities.resume_entities.reviews_entities;

import com.rebuild.backend.model.entities.user_entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "resume_reviews")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class ResumeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "content")
    @NonNull
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST
    })
    @JoinColumn(name = "snapshot_node_id", referencedColumnName = "id")
    private ResumeSnapshotNode snapshotNode;


    @ManyToOne(fetch = FetchType.LAZY, cascade = {
            CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST
    })
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User reviewingUser;
}
