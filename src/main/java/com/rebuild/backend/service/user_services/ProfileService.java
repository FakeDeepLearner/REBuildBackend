package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.user_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.FriendRelationship;
import com.rebuild.backend.model.entities.user_entities.InformationVisibility;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final ProfileHelperService helperService;

    @Autowired
    public ProfileService(UserRepository userRepository, FriendRelationshipRepository friendRelationshipRepository,
                         ProfileHelperService helperService) {
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
        this.helperService = helperService;
    }


    public UserProfileResponse loadSelfProfile(User user)
    {
        return new UserProfileResponse(
                new ProfileSensitiveInformationDTO(user.getImageUrl(),
                        user.getEmail(), user.getForumUsername(),
                        user.getName(), user.getPhoneNumber()),
                helperService.loadCommentDTOsForUser(user),
                helperService.loadPostDTOsForUser(user)
        );
    }

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
                loadOtherUserProfile(foundUser, foundRelationship.isPresent());

    }

    private InformationVisibility mapStringToVisibility(String input)
    {
        return switch (input){
            case "Everyone" -> InformationVisibility.EVERYONE;
            case "Friends Only" -> InformationVisibility.FRIENDS_ONLY;
            case "No One" -> InformationVisibility.NO_ONE;
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid input");
        };
    }

    public UserProfileResponse changeProfilePrivacySettings(User user,
                                                            ProfilePrivacySettingsForm privacySettingsForm)
    {
        InformationVisibility postsVisibility = mapStringToVisibility(privacySettingsForm.postsVisibilityValue());
        InformationVisibility commentsVisibility = mapStringToVisibility(privacySettingsForm.commentsVisibilityValue());
        InformationVisibility sensitiveInfoVisibility = mapStringToVisibility(privacySettingsForm.sensitiveInfoVisibilityValue());

        user.setPostsVisibility(postsVisibility);
        user.setCommentsVisibility(commentsVisibility);
        user.setSensitiveInfoVisibility(sensitiveInfoVisibility);
        user.setMessagesFromFriendsOnly(privacySettingsForm.messagesFromFriends());

        User savedUser = userRepository.save(user);
        return loadSelfProfile(savedUser);
    }

}
