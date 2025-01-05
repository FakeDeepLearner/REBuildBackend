package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    int countByIdAndUserId(UUID id, UUID userId);
}
