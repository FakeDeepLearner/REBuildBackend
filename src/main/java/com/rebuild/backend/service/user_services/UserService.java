package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.user_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.entities.messaging_and_friendship_entities.Friendship;
import com.rebuild.backend.model.entities.user_entities.InformationVisibility;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.model.responses.user_responses.UsernameSearchResponse;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final ProfileHelperService helperService;

    @Autowired
    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository,
                       ProfileHelperService helperService) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.helperService = helperService;
    }


    public UserProfileResponse loadSelfProfile(User user)
    {
        return new UserProfileResponse(
                new ProfileSensitiveInformationDTO(user.getImageUrl(),
                        user.getEmail(), user.getForumUsername(),
                        user.getName(), user.getPhoneNumber(),
                        user.getLocation()),
                user.getBiography(),
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


        UserPair userPair = new UserPair(foundUser, user);

        return helperService.
                loadOtherUserProfile(foundUser, friendshipRepository.
                        existsByLowUserIdAndHighUserId(userPair.lowId(), userPair.highId()));
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

    public String updateUserLocation(User user, String newLocation)
    {
        if (newLocation.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Location may not be blank");
        }

        user.setLocation(newLocation);

        User savedUser = userRepository.save(user);
        return savedUser.getLocation();
    }

    public String updateUserBiography(User user, String newBiography)
    {
        if (newBiography.isBlank())
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Biography may not be blank");
        }

        user.setBiography(newBiography);

        User savedUser = userRepository.save(user);
        return savedUser.getBiography();
    }

    public UsernameSearchResponse getUsernameSearchResults(String username, User searchingUser,
                                                           int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }
        PageRequest request = PageRequest.of(pageNumber, 10);
        Slice<User> foundUsers = userRepository.findBySimilarUsername(username, request);
        List<UsernameSearchResultDTO> searchResultDTOS =
                foundUsers.stream()
                        .map(user -> {
                            UserPair userPair = new UserPair(searchingUser, user);

                            return new UsernameSearchResultDTO(user.getId(), user.getForumUsername(),
                                    friendshipRepository.existsByLowUserIdAndHighUserId(userPair.lowId(),
                                            userPair.highId()));
                        }).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS, foundUsers.getNumber(), foundUsers.hasNext());
    }
}
