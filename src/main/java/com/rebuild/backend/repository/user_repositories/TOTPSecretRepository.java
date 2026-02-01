package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.TOTPSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TOTPSecretRepository extends JpaRepository<TOTPSecret, UUID> {
}
