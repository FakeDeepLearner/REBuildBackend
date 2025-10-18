package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostSearchRepository extends JpaRepository<PostSearchConfiguration, UUID> {


}
