package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.ResumeExperience;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<ResumeExperience, UUID> {


    Optional<ResumeExperience> findByIdAndResume_IdAndResume_User(UUID id, UUID resumeId, User resumeUser);
}
