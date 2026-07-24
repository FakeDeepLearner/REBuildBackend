package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.ForumPostSummaryDTO;
import com.rebuild.backend.model.dtos.user_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.responses.forum_responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.user_responses.UsernameSearchResponse;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.utils.UserPair;
import com.rebuild.backend.utils.exceptions.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ForumHomePageService {
    private final ForumPostRepository postRepository;

    private final UserRepository userRepository;

    private final FriendshipRepository friendshipRepository;

    @Autowired
    public ForumHomePageService(ForumPostRepository postRepository,
                               UserRepository userRepository,
                                FriendshipRepository friendshipRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    public ForumPostPageResponse loadForum(int pageNumber, User user)
    {
        return loadForumWithSpecsForm(pageNumber, new ForumSpecsForm(null, null), user);
    }

    public ForumPostPageResponse loadForumWithSpecsForm(int pageNumber,
                                                        ForumSpecsForm forumSpecsForm, User user)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }
        PageRequest request = PageRequest.of(pageNumber, 10);

        Slice<ForumPostSummaryDTO> foundPosts = postRepository.findByTitleAndContent(forumSpecsForm.titleContains(),
                forumSpecsForm.bodyContains(),
                user.getId(),
                request);

        return new ForumPostPageResponse(foundPosts.getContent(), foundPosts.getNumber(), foundPosts.hasNext());
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
                                    friendshipRepository.findByLowUserIdAndHighUserId(userPair.lowId(),
                                            userPair.highId()).isPresent());
                        }).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS, foundUsers.getNumber(), foundUsers.hasNext());
    }
}
