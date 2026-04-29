package com.rebuild.backend.repository.resume_repositories.reviews_repositories;

import com.rebuild.backend.model.entities.resume_entities.reviews_entities.ResumeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
interface ResumeReviewRepository extends JpaRepository<ResumeReview, UUID> {
}
