package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.TemporaryMFASecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemporaryMFASecretRepository extends JpaRepository<TemporaryMFASecret, UUID> {

    Optional<TemporaryMFASecret> findByEmail(String email);
}
