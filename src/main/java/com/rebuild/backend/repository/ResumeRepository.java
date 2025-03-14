package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.*;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID>, JpaSpecificationExecutor<Resume> {

    void deleteById(@NonNull UUID ID);

    int countByIdAndUserId(UUID id, UUID userID);

    @NonNull
    Page<Resume> findAllById(UUID id,  @NonNull Pageable pageable);


}
