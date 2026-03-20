package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.profile_entities.InformationVisibility;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.service.util_services.CloudinaryService;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfileHelperService {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public ProfileHelperService(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    private ProfileSensitiveInformationDTO decideSensitiveInfo(User user, boolean thereIsFriendship)
    {
        UserProfile profile = user.getUserProfile();
        InformationVisibility sensitiveInfoVisibility = profile.getSensitiveInfoVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE) ||
                    sensitiveInfoVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPicture(profile.getProfilePicture()),
                        user.getEmail(), user.getPhoneNumber());
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE))
            {
                return new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPicture(profile.getProfilePicture()),
                        user.getEmail(), user.getPhoneNumber());
            }

            //Otherwise, return the information masked
        }
        return new ProfileSensitiveInformationDTO(null,
                StringUtil.maskString(user.getEmail()), StringUtil.maskString(user.getPhoneNumber()));
    }

    private List<Comment> decideCommentList(User user, boolean thereIsFriendship)
    {
        UserProfile profile = user.getUserProfile();
        InformationVisibility commentsVisibility = profile.getCommentsVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (commentsVisibility.equals(InformationVisibility.EVERYONE) ||
                    commentsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return user.getMadeComments();
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (commentsVisibility.equals(InformationVisibility.EVERYONE))
            {
                return user.getMadeComments();
            }

            //Otherwise, return the information masked
        }
        return null;
    }

    private List<ForumPost> decidePostsList(User user, boolean thereIsFriendship)
    {
        UserProfile profile = user.getUserProfile();
        InformationVisibility postsVisibility = profile.getPostsVisibility();
        if (thereIsFriendship)
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE) ||
                    postsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return user.getMadePosts();
            }
        }

        else
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE))
            {
                return user.getMadePosts();
            }
        }
        return null;
    }


    public UserProfileResponse loadOtherUserProfile(User otherUser, boolean thereIsFriendship)
    {
        List<ForumPost> postsList = decidePostsList(otherUser, thereIsFriendship);
        List<Comment> commentsList = decideCommentList(otherUser, thereIsFriendship);

        ProfileSensitiveInformationDTO sensitiveInformationDTO = decideSensitiveInfo(otherUser, thereIsFriendship);

        return new UserProfileResponse(sensitiveInformationDTO, otherUser.getForumUsername(), commentsList, postsList);
    }
}
