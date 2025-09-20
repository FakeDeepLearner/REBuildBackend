package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.users.CaptchaVerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaptchaVerificationRepository extends JpaRepository<CaptchaVerificationRecord, UUID> {
}
