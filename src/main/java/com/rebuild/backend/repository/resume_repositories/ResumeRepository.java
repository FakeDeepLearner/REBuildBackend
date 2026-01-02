package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    Optional<Resume> findByIdAndUser(UUID id, User user);

    @EntityGraph(value = Resume.GRAPH_NAME, type =  EntityGraph.EntityGraphType.LOAD)
    List<Resume> findByUserAndIdIn(User user, Collection<UUID> ids);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.header
        LEFT JOIN FETCH r.education
        LEFT JOIN FETCH r.experiences
        LEFT JOIN FETCH r.projects
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithOtherData(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.header
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithHeader(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
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
}
