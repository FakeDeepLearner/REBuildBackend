package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeSearchRepository extends JpaRepository<ResumeSearchConfiguration, UUID> {


    Optional<ResumeSearchConfiguration> findByIdAndUser(UUID id, User user);

    List<ResumeSearchConfiguration> findAllByUser(User user, Sort sort);


}
