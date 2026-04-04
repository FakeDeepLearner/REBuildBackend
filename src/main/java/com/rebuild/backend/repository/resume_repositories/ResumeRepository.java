package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {


    Optional<Resume> findByIdAndUser(UUID id, User user);

    @Query("""
        SELECT r FROM Resume r
        JOIN FETCH r.header
        JOIN FETCH r.education
        JOIN FETCH r.projects
        JOIN FETCH r.experiences
        WHERE r.user=?1 AND r.id IN ?2
        """)
    List<Resume> findByUserAndIdIn(User user, Collection<UUID> ids);

    @Query(value = """
        SELECT r FROM Resume r
        LEFT JOIN FETCH r.header
        LEFT JOIN FETCH r.education
        LEFT JOIN FETCH r.experiences
        LEFT JOIN FETCH r.projects
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithOtherData(UUID id, User user);

    @Query(value = """
        SELECT r FROM Resume r
        LEFT JOIN FETCH r.header
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithHeader(UUID id, User user);

    @Query(value = """
        SELECT r FROM Resume r
        LEFT JOIN FETCH r.education
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithEducation(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.experiences
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithExperiences(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.projects
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithProjects(UUID id, User user);

    Page<Resume> findByUser(User user, Pageable pageable);

    Page<Resume> findByUserAndIdIn(User user, Collection<UUID> ids, Pageable pageable);
}
