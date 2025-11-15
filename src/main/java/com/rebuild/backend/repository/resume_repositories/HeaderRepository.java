package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.resume_entities.Education;
import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeaderRepository extends JpaRepository<Header, UUID> {

    Optional<Header> findByIdAndProfile(UUID id, UserProfile profile);

    Optional<Header> findByIdAndResume(UUID id, Resume resume);
}
