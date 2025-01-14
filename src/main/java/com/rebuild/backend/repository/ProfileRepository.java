package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

}
