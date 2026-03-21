package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.profile_entities.*;
import com.rebuild.backend.model.entities.resume_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePreferencesForm;
import com.rebuild.backend.model.forms.resume_forms.*;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import com.rebuild.backend.service.util_services.SubpartsModificationService;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.database_utils.YearMonthStringOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.print.DocFlavor;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final CloudinaryService cloudinaryService;

    private final ProfileHelperService helperService;

    @Autowired
    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository, FriendRelationshipRepository friendRelationshipRepository,
                          CloudinaryService cloudinaryService, ProfileHelperService helperService) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.cloudinaryService = cloudinaryService;
        this.helperService = helperService;
    }

    @Transactional
    public UserProfileResponse loadUserProfile(User user, UUID clickedUserId)
    {
        UserProfile associatedProfile = profileRepository.findByUserId(clickedUserId);
        // If you are trying to load your own profile, simply get
        // all the information regardless of info visibility settings
        if (clickedUserId.equals(user.getId()))
        {

            return new UserProfileResponse(
                    new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPicture(associatedProfile.getProfilePicture()),
                    user.getEmail(), user.getPhoneNumber()),
                    user.getForumUsername(), associatedProfile.getMadeComments(), associatedProfile.getMadePosts()
                    );
        }


        User foundUser = userRepository.findById(clickedUserId).orElse(null);

        assert foundUser != null : "User not found";

        Optional<FriendRelationship> foundRelationship = friendRelationshipRepository.
                findByUserAndUserId(user, clickedUserId);

        return helperService.loadOtherUserProfile(foundUser, associatedProfile, foundRelationship.isPresent());

    }

}
