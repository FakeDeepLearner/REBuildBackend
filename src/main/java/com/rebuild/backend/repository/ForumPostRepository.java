package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID>, JpaSpecificationExecutor<ForumPost> {

    int countByIdAndUserId(UUID id, UUID userId);

    @NonNull
    Page<ForumPost> findAll(@NonNull Pageable pageable);
}
