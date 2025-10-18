package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

}
