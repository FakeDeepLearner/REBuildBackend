package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {


    @Query(value = """
        SELECT rv FROM ResumeVersion rv
        LEFT JOIN FETCH rv.versionedHeader
        LEFT JOIN FETCH rv.versionedEducation
        LEFT JOIN FETCH rv.versionedExperiences
        WHERE rv.id=?1 AND rv.associatedResume=?2
        """)
    Optional<ResumeVersion> findByIdAndAssociatedResume(UUID versionID, Resume associatedResume);

}
