package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePreferencesForm;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import com.rebuild.backend.service.util_services.SubpartsModificationService;
import com.rebuild.backend.utils.database_utils.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository,
                          CloudinaryService cloudinaryService) {
        this.profileRepository = profileRepository;
        this.cloudinaryService = cloudinaryService;
    }



    @Transactional
    public void deleteProfile(User deletingUser){
        UserProfile profile = cloudinaryService.removeProfilePicture(deletingUser, true);

        profileRepository.delete(profile);
    }


}
