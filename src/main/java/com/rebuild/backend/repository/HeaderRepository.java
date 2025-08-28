package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.resume_entities.Header;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HeaderRepository extends JpaRepository<Header, UUID> {
}
