package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.MFARecoveryCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecoveryCodeRepository extends JpaRepository<MFARecoveryCodeEntity, UUID> {
}
