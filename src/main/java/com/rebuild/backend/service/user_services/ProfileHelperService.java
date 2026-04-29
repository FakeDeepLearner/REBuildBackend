package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.InformationVisibility;
import com.rebuild.backend.model.entities.user_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.UserProfileResponse;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.service.util_services.CloudinaryService;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfileHelperService {

    private final CloudinaryService cloudinaryService;

    private final CommentRepository commentRepository;

    private final ForumPostRepository forumPostRepository;

    @Autowired
    public ProfileHelperService(CloudinaryService cloudinaryService,
                                CommentRepository commentRepository, ForumPostRepository forumPostRepository) {
        this.cloudinaryService = cloudinaryService;
        this.commentRepository = commentRepository;
        this.forumPostRepository = forumPostRepository;
    }

    private ProfileSensitiveInformationDTO decideSensitiveInfo(User user, UserProfile profile, boolean thereIsFriendship)
    {
        InformationVisibility sensitiveInfoVisibility = profile.getSensitiveInfoVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE) ||
                    sensitiveInfoVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPictureId(profile.getPictureId()),
                        user.getEmail(), user.getPhoneNumber());
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE))
            {
                return new ProfileSensitiveInformationDTO(cloudinaryService.generateTimedUrlForPictureId(profile.getPictureId()),
                        user.getEmail(), user.getPhoneNumber());
            }

            //Otherwise, return the information masked
        }
        return new ProfileSensitiveInformationDTO(null,
                StringUtil.maskString(user.getEmail()), StringUtil.maskString(user.getPhoneNumber()));
    }

    private List<Comment> decideCommentList(User user, UserProfile profile, boolean thereIsFriendship)
    {
        InformationVisibility commentsVisibility = profile.getCommentsVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (commentsVisibility.equals(InformationVisibility.EVERYONE) ||
                    commentsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return commentRepository.findByUserUnAnonymized(user);
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (commentsVisibility.equals(InformationVisibility.EVERYONE))
            {
                return commentRepository.findByUserUnAnonymized(user);
            }

            //Otherwise, return the information masked
        }
        return null;
    }

    private List<ForumPost> decidePostsList(User user, UserProfile profile, boolean thereIsFriendship)
    {
        InformationVisibility postsVisibility = profile.getPostsVisibility();
        if (thereIsFriendship)
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE) ||
                    postsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return forumPostRepository.findByUserUnAnonymized(user);
            }
        }

        else
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE))
            {
                return forumPostRepository.findByUserUnAnonymized(user);
            }
        }
        return null;
    }


    public UserProfileResponse loadOtherUserProfile(User otherUser, UserProfile profile, boolean thereIsFriendship)
    {
        List<ForumPost> postsList = decidePostsList(otherUser, profile, thereIsFriendship);
        List<Comment> commentsList = decideCommentList(otherUser, profile, thereIsFriendship);

        ProfileSensitiveInformationDTO sensitiveInformationDTO = decideSensitiveInfo(otherUser,
                profile, thereIsFriendship);

        return new UserProfileResponse(sensitiveInformationDTO, otherUser.getForumUsername(), commentsList, postsList);
    }
}
