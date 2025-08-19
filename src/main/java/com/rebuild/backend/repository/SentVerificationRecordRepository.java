package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.users.SentVerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SentVerificationRecordRepository extends JpaRepository<SentVerificationRecord, UUID> {

}
