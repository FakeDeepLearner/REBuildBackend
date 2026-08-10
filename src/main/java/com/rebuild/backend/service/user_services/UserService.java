package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationDisplayDTO;
import com.rebuild.backend.model.dtos.user_dtos.ChatApplicationFetchDTO;
import com.rebuild.backend.model.dtos.user_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.enums.InformationVisibility;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.profile_forms.ProfilePrivacySettingsForm;
import com.rebuild.backend.model.responses.user_responses.ChatApplicationSearchResponse;
import com.rebuild.backend.model.responses.user_responses.UsernameSearchResponse;
import com.rebuild.backend.repository.chat_repositories.ChatApplicationRepository;
import com.rebuild.backend.utils.StringUtil;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.repository.user_repositories.FriendshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    private final ChatApplicationRepository chatApplicationRepository;

    @Autowired
    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository,
                       ChatApplicationRepository chatApplicationRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.chatApplicationRepository = chatApplicationRepository;
    }


    public UserProfileResponse loadSelfProfile(User user)
    {
        return new UserProfileResponse(
                new ProfileSensitiveInformationDTO(user.getImageUrl(),
                        user.getEmail(), user.getForumUsername(),
                        user.getName(),
                        user.getLocation()),
                user.getBiography()
        );
    }

    private ProfileSensitiveInformationDTO decideSensitiveInfo(User user, boolean thereIsFriendship)
    {
        InformationVisibility sensitiveInfoVisibility = user.getSensitiveInfoVisibility();
        if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE))
        {
            return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                    user.getEmail(), user.getForumUsername(),
                    user.getName(),
                    user.getLocation());
        }
        if (thereIsFriendship && sensitiveInfoVisibility.equals(InformationVisibility.FRIENDS_ONLY))
        {
            return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                    user.getEmail(), user.getForumUsername(),
                    user.getName(),
                    user.getLocation());
        }

        return new ProfileSensitiveInformationDTO(null,
                StringUtil.maskString(user.getEmail()), user.getForumUsername(),
                StringUtil.maskString(user.getName()),
                StringUtil.maskString(user.getLocation()));
    }


    public UserProfileResponse loadOtherUserProfile(User otherUser, boolean thereIsFriendship)
    {
        ProfileSensitiveInformationDTO sensitiveInformationDTO = decideSensitiveInfo(otherUser,
                thereIsFriendship);

        return new UserProfileResponse(sensitiveInformationDTO, otherUser.getBiography());
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

        return loadOtherUserProfile(foundUser, friendshipRepository.
                        existsByLowUserIdAndHighUserId(userPair.lowId(), userPair.highId()));
    }

    public UserProfileResponse changeProfilePrivacySettings(User user,
                                                            ProfilePrivacySettingsForm privacySettingsForm)
    {
        InformationVisibility sensitiveInfoVisibility =
                InformationVisibility.fromValue(privacySettingsForm.sensitiveInfoVisibilityValue());
        if (sensitiveInfoVisibility == null)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid input");
        }
        user.setSensitiveInfoVisibility(sensitiveInfoVisibility);

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

    public ChatApplicationSearchResponse getAllChatApplications(User searchingUser, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }

        PageRequest request = PageRequest.of(pageNumber, 10,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Slice<ChatApplicationFetchDTO> foundResults = chatApplicationRepository.findByAssociatedUser(searchingUser,
                request);

        List<ChatApplicationDisplayDTO> displayDTOS = foundResults.stream().map(ChatApplicationFetchDTO::toDisplayDTO)
                .toList();

        return new ChatApplicationSearchResponse(displayDTOS, foundResults.getNumber(), foundResults.hasNext());
    }
}
