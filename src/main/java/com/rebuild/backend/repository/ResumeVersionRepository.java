package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.ResumeVersion;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"versionedHeader", "versionedEducation", "versionedExperiences",
    "versionedSections"})
    Optional<ResumeVersion> findById(@NonNull UUID uuid);
}
