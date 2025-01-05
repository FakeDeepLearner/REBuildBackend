package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
}
