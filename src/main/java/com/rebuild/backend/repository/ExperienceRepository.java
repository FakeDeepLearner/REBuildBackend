package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.Experience;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface ExperienceRepository extends CrudRepository<Experience, UUID> {

}
