package com.rebuild.backend.repository;

import com.rebuild.backend.model.Experience;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExperienceRepository extends CrudRepository<Experience, UUID> {

}
