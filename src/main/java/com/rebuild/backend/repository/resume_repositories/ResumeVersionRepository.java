package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.entities.versioning_entities.ResumeVersion;
import jakarta.persistence.NamedEntityGraph;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {

    @EntityGraph(value = ResumeVersion.GRAPH_NAME, type = EntityGraph.EntityGraphType.LOAD)
    Optional<ResumeVersion> findByIdAndAssociatedResume_IdAndAssociatedResume_User(UUID id,
                                                                                   UUID associatedResumeId,
                                                                                   User associatedResumeUser);

}
