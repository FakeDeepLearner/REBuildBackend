package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, UUID> {


    void deleteProfilePictureByPublic_id(String public_id);

    @Query(value = "SELECT p FROM ProfilePicture p WHERE " +
            "p.associatedProfile.user.id=?1"
    )
    Optional<ProfilePicture> findByUserId(UUID userId);
}
