package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.ProfileRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



    public UserProfileResponse loadSelfProfile(User user)
    {
        UserProfile associatedProfile = user.getUserProfile();
        return new UserProfileResponse(
                new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPictureId(associatedProfile.getPictureId()),
                        user.getEmail(), user.getPhoneNumber()),
                user.getForumUsername(), user.getMadeComments(), user.getMadePosts()
        );
    }

    @Transactional
    public UserProfileResponse loadUserProfile(User user, UUID clickedUserId)
    {

        if (clickedUserId.equals(user.getId()))
        {
            return loadSelfProfile(user);
        }

        User foundUser = userRepository.findById(clickedUserId).orElseThrow(() ->
                new NotFoundException("User with this id is not found"));


        Optional<FriendRelationship> foundRelationship = friendRelationshipRepository.
                findByUserAndUserId(user, clickedUserId);

        return helperService.
                loadOtherUserProfile(foundUser, foundUser.getUserProfile(), foundRelationship.isPresent());

    }

}
