package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.ProfileHistoryCommentDTO;
import com.rebuild.backend.model.dtos.ProfileHistoryPostDTO;
import com.rebuild.backend.model.dtos.forum_dtos.ProfileSensitiveInformationDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.InformationVisibility;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.responses.user_responses.UserProfileResponse;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProfileHelperService {

    private final CommentRepository commentRepository;

    private final ForumPostRepository forumPostRepository;

    @Autowired
    public ProfileHelperService(CommentRepository commentRepository, ForumPostRepository forumPostRepository){
        this.commentRepository = commentRepository;
        this.forumPostRepository = forumPostRepository;
    }

    public List<ProfileHistoryCommentDTO> loadCommentDTOsForUser(User user)
    {
        List<Comment> madeComments = commentRepository.findByUserAndNotDeleted(user);
        return madeComments.stream().map(comment ->
                new ProfileHistoryCommentDTO(comment.getContent(), comment.getCreatedAt())).toList();
    }


    public List<ProfileHistoryPostDTO> loadPostDTOsForUser(User user)
    {
        List<ForumPost> userPosts = forumPostRepository.findByUserOrdered(user);
        return userPosts.stream().map(forumPost -> new ProfileHistoryPostDTO(forumPost.getTitle(), forumPost.getContent(),
                forumPost.getCreatedAt())).toList();
    }

    private ProfileSensitiveInformationDTO decideSensitiveInfo(User user, boolean thereIsFriendship)
    {
        InformationVisibility sensitiveInfoVisibility = user.getSensitiveInfoVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE) ||
                    sensitiveInfoVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                        user.getEmail(), user.getForumUsername(),
                        StringUtil.maskString(user.getName()), StringUtil.maskString(user.getPhoneNumber()));
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE))
            {
                return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                        user.getEmail(), user.getForumUsername(),
                        StringUtil.maskString(user.getName()), StringUtil.maskString(user.getPhoneNumber()));
            }

            //Otherwise, return the information masked
        }
        return new ProfileSensitiveInformationDTO(null,
                StringUtil.maskString(user.getEmail()), user.getForumUsername(),
                StringUtil.maskString(user.getName()), StringUtil.maskString(user.getPhoneNumber()));
    }

    private List<ProfileHistoryCommentDTO> decideCommentList(User user, boolean thereIsFriendship)
    {
        InformationVisibility commentsVisibility = user.getCommentsVisibility();
        if (thereIsFriendship)
        {
            //If the user has selected their information to be visible to everyone or to friends only, return it normally
            if (commentsVisibility.equals(InformationVisibility.EVERYONE) ||
                    commentsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return loadCommentDTOsForUser(user);
            }
            //Otherwise, return the information masked
        }

        //If there is no friendship, we will only return the information properly if the visibility is set to everyone
        else
        {
            if (commentsVisibility.equals(InformationVisibility.EVERYONE))
            {
                return loadCommentDTOsForUser(user);
            }

            //Otherwise, return the information masked
        }
        return null;
    }

    private List<ProfileHistoryPostDTO> decidePostsList(User user, boolean thereIsFriendship)
    {

        InformationVisibility postsVisibility = user.getPostsVisibility();
        if (thereIsFriendship)
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE) ||
                    postsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
            {
                return loadPostDTOsForUser(user);
            }
        }

        else
        {
            if (postsVisibility.equals(InformationVisibility.EVERYONE))
            {
               return loadPostDTOsForUser(user);
            }
        }
        return null;
    }


    public UserProfileResponse loadOtherUserProfile(User otherUser, boolean thereIsFriendship)
    {
        List<ProfileHistoryPostDTO> postsList = decidePostsList(otherUser, thereIsFriendship);
        List<ProfileHistoryCommentDTO> commentsList = decideCommentList(otherUser, thereIsFriendship);

        ProfileSensitiveInformationDTO sensitiveInformationDTO = decideSensitiveInfo(otherUser,
                thereIsFriendship);

        return new UserProfileResponse(sensitiveInformationDTO, commentsList, postsList);
    }
}
