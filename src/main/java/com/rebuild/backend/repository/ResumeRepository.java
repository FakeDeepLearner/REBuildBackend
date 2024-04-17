package com.rebuild.backend.repository;

import com.rebuild.backend.model.Resume;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ResumeRepository extends CrudRepository<Resume, UUID> {
}
