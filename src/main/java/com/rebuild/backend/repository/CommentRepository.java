package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<@NonNull Comment, @NonNull UUID> {

    int countByIdAndUserId(UUID id, UUID userId);

    List<Comment> findByParentIdOrderByCreationDateAsc(UUID parentId);
}
