package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUser(User user);


    @Query("""
    SELECT p FROM UserProfile p
    LEFT JOIN FETCH p.madeComments LEFT JOIN FETCH p.madePosts
    WHERE p.user.id=:id
    """)
    Optional<UserProfile> findByUserId(UUID id);

}
