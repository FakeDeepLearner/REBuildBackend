package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@CacheConfig(cacheManager = "cacheManager", cacheNames = "profile_cache",
keyGenerator = "profileCacheKeyGenerator")
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

    @Query(
    """
    SELECT p FROM UserProfile p
    JOIN FETCH p.header JOIN FETCH p.education JOIN FETCH p.experienceList JOIN FETCH p.projectList
    WHERE p.user=?1
"""
    )
    @Cacheable
    UserProfile findByUserWithAllData(User user);


    @Query(
            """
            SELECT p FROM UserProfile p
            JOIN FETCH p.header
            WHERE p.user=?1
        """
    )
    @Cacheable
    UserProfile findByUserWithHeader(User user);

    @Query(
            """
            SELECT p FROM UserProfile p
            JOIN FETCH p.education
            WHERE p.user=?1
        """
    )
    @Cacheable
    UserProfile findByUserWithEducation(User user);

    @Query(
            """
            SELECT p FROM UserProfile p
            JOIN FETCH p.experienceList
            WHERE p.user=?1
        """
    )
    @Cacheable
    UserProfile findByUserWithExperiences(User user);

    @Query(
            """
            SELECT p FROM UserProfile p
            JOIN FETCH p.projectList
            WHERE p.user=?1
        """
    )
    @Cacheable
    UserProfile findByUserWithProjects(User user);




    @Cacheable
    UserProfile findByUser(User user);

}
