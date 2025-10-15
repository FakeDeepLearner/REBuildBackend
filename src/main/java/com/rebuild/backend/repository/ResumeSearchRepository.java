package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.ResumeSearchConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResumeSearchRepository extends JpaRepository<ResumeSearchConfiguration, UUID> {
}
