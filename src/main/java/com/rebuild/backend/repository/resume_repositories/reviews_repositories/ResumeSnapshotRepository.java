package com.rebuild.backend.repository.resume_repositories.reviews_repositories;

import com.rebuild.backend.model.entities.resume_entities.reviews_entities.ResumeSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ResumeSnapshotRepository extends JpaRepository<ResumeSnapshot, UUID> {
}
