package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByIdAndUser(UUID id, User user);

}
