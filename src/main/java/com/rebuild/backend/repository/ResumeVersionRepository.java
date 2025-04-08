package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {

    Optional<ResumeVersion> findOldestVersionByResumeId(UUID resumeId);

}
