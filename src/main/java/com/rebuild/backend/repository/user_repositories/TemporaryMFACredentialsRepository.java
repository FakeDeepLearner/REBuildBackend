package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.TemporaryMFACredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemporaryMFACredentialsRepository extends JpaRepository<TemporaryMFACredentials, UUID> {



    Optional<TemporaryMFACredentials> findByEmailAndExpiryTimeAfter(String email,
                                                                    Instant expiryTimeAfter);
}
