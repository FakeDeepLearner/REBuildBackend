package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.profile_entities.ProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfilePictureRepository extends JpaRepository<ProfilePicture, UUID> {

    void deleteProfilePictureByAsset_id(String asset_id);

    void deleteProfilePictureByPublic_id(String public_id);
}
