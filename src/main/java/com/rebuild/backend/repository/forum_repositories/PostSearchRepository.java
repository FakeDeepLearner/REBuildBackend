package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostSearchRepository extends JpaRepository<PostSearchConfiguration, UUID> {

    Optional<PostSearchConfiguration> findByIdAndUser(UUID id, User user);


    List<PostSearchConfiguration> findAllByUser(User user);


}
