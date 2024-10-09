package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(@NonNull UUID userId);

    void deleteUserProfileByUserId(@NonNull UUID id);

    void deleteProfileHeaderByUserId(@NonNull UUID id);

    void deleteProfileEducationByUserId(@NonNull UUID id);

    void deleteProfileExperiencesByUserId(@NonNull UUID id);


}
