package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface ProfileRepository extends JpaRepository<UserProfile, UUID> {

    void deleteProfileHeaderById(@NonNull UUID id);

    void deleteProfileEducationById(@NonNull UUID id);

    void deleteProfileExperiencesById(@NonNull UUID id);

    void deleteProfileSectionsById(@NonNull UUID id);


}
