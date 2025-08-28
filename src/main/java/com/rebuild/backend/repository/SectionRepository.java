package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.ResumeSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SectionRepository extends JpaRepository<ResumeSection, UUID> {
}
