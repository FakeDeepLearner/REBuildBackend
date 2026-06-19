package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {


    Optional<User> findByEmail(String email);

    @Query(value = """
    SELECT u FROM users u
    WHERE to_tsvector('english', u.forum_username) @@ websearch_to_tsquery('english', ?1)
    """, nativeQuery = true)
    Slice<User> findBySimilarUsername(String username, Pageable pageable);






}
