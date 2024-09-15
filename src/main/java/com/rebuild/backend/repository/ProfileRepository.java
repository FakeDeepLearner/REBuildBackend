package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProfileRepository extends CrudRepository<UserProfile, UUID> {

    void deleteUserProfileById(@NonNull UUID id);

    void deleteUserProfileByUserId(@NonNull UUID id);


}
