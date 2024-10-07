package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {
}
