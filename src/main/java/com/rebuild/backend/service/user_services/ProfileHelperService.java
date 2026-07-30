package com.rebuild.backend.service.user_services;

import com.rebuild.backend.model.dtos.user_dtos.ProfileHistoryCommentDTO;
import com.rebuild.backend.model.dtos.user_dtos.ProfileHistoryPostDTO;
import com.rebuild.backend.model.dtos.user_dtos.ProfileSensitiveInformationDTO;
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
        if (sensitiveInfoVisibility.equals(InformationVisibility.EVERYONE))
        {
            return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                    user.getEmail(), user.getForumUsername(),
                    user.getName(), user.getPhoneNumber(),
                    user.getLocation());
        }
        if (thereIsFriendship && sensitiveInfoVisibility.equals(InformationVisibility.FRIENDS_ONLY))
        {
            return new ProfileSensitiveInformationDTO(user.getImageUrl(),
                    user.getEmail(), user.getForumUsername(),
                    user.getName(), user.getPhoneNumber(),
                    user.getLocation());
        }

        return new ProfileSensitiveInformationDTO(null,
                StringUtil.maskString(user.getEmail()), user.getForumUsername(),
                StringUtil.maskString(user.getName()), StringUtil.maskString(user.getPhoneNumber()),
                StringUtil.maskString(user.getLocation()));
    }

    private List<ProfileHistoryCommentDTO> decideCommentList(User user, boolean thereIsFriendship)
    {
        InformationVisibility commentsVisibility = user.getCommentsVisibility();
        if (commentsVisibility.equals(InformationVisibility.EVERYONE))
        {
            return loadCommentDTOsForUser(user);
        }
        if (thereIsFriendship && commentsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
        {
            return loadCommentDTOsForUser(user);
        }

        return null;
    }

    private List<ProfileHistoryPostDTO> decidePostsList(User user, boolean thereIsFriendship)
    {

        InformationVisibility postsVisibility = user.getPostsVisibility();
        if (postsVisibility.equals(InformationVisibility.EVERYONE))
        {
            return loadPostDTOsForUser(user);
        }
        if (thereIsFriendship && postsVisibility.equals(InformationVisibility.FRIENDS_ONLY))
        {
            return loadPostDTOsForUser(user);
        }

        return null;
    }


    public UserProfileResponse loadOtherUserProfile(User otherUser, boolean thereIsFriendship)
    {
        List<ProfileHistoryPostDTO> postsList = decidePostsList(otherUser, thereIsFriendship);
        List<ProfileHistoryCommentDTO> commentsList = decideCommentList(otherUser, thereIsFriendship);

        ProfileSensitiveInformationDTO sensitiveInformationDTO = decideSensitiveInfo(otherUser,
                thereIsFriendship);

        return new UserProfileResponse(sensitiveInformationDTO, otherUser.getBiography(),
                commentsList, postsList);
    }
}
