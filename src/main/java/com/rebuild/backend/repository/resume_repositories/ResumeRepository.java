package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    Optional<Resume> findByIdAndUser(UUID id, User user);


    @EntityGraph(attributePaths = {
            "header",
            "education",
            "experiences"}
    )
    @Query("""
        SELECT r FROM Resume r WHERE r.id IN ?1
        """)
    List<Resume> findAllByIdWithOtherData(Iterable<UUID> ids);


    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.header
        LEFT JOIN FETCH r.education
        LEFT JOIN FETCH r.experiences
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithOtherData(UUID id, User user);
}
