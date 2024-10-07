package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForumPostRepository extends CrudRepository<ForumPost, UUID> {

}
