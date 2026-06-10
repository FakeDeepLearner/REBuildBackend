package com.rebuild.backend.repository.resume_repositories;

import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.resume_responses.ResumePreviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {


    Optional<Resume> findByIdAndUser(UUID id, User user);

    @Query("""
        SELECT DISTINCT r FROM Resume r
        JOIN FETCH r.resumeHeader
        JOIN FETCH r.resumeEducation
        JOIN FETCH r.resumeProjects
        JOIN FETCH r.resumeExperiences
        WHERE r.user=?1 AND r.id IN ?2
        """)
    List<Resume> findByUserAndIdIn(User user, Collection<UUID> ids);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.resumeHeader
        LEFT JOIN FETCH r.resumeEducation
        LEFT JOIN FETCH r.resumeExperiences
        LEFT JOIN FETCH r.resumeProjects
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithOtherData(UUID id, User user);

    @Query(value = """
        SELECT r FROM Resume r
        LEFT JOIN FETCH r.resumeHeader
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithHeader(UUID id, User user);

    @Query(value = """
        SELECT r FROM Resume r
        LEFT JOIN FETCH r.resumeEducation
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithEducation(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.resumeExperiences
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithExperiences(UUID id, User user);

    @Query(value = """
        SELECT DISTINCT r FROM Resume r
        LEFT JOIN FETCH r.resumeProjects
        WHERE r.id=?1 AND r.user=?2
       """)
    Optional<Resume> findByIdAndUserWithProjects(UUID id, User user);

    @Query(value = """
    SELECT r.id, r.resume_name, r.preview_url
    FROM resumes r
    WHERE (?2 IS NULL OR to_tsvector('english', r.resume_name) @@ websearch_to_tsquery('english', ?2))
    AND r.user=?1
    """, nativeQuery = true)
    Slice<ResumePreviewResponse> findByUserAndNameContaining(User user, String name, Pageable pageable);


    Optional<Resume> findByUserAndName(User user, String name);



}
