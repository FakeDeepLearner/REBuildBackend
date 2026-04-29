package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.ForumPostSummaryDTO;
import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.UsernameSearchResponse;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.messaging_and_friendship_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForumHomePageService {
    private final ForumPostRepository postRepository;

    private final UserRepository userRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    @Autowired
    public ForumHomePageService(ForumPostRepository postRepository,
                               UserRepository userRepository,
                                FriendRelationshipRepository friendRelationshipRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.friendRelationshipRepository = friendRelationshipRepository;
    }

    public ForumPostPageResponse serveGetRequest(int pageNumber, int pageSize, User user)
    {
        return getPagedResult(pageNumber, pageSize, new ForumSpecsForm(null, null), user);
    }

    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                ForumSpecsForm forumSpecsForm, User user)
    {
        PageRequest request = PageRequest.of(pageNumber, pageSize);

        Slice<ForumPostSummaryDTO> foundPosts = postRepository.findByTitleAndContent(forumSpecsForm.titleContains(),
                forumSpecsForm.bodyContains(),
                user.getId(),
                request);

        return new ForumPostPageResponse(foundPosts.getContent(), foundPosts.getNumber(), foundPosts.hasNext());
    }

    public UsernameSearchResponse getUsernameSearchResults(String username, User searchingUser,
                                                           int pageNumber, int pageSize)
    {
        PageRequest request = PageRequest.of(pageNumber, pageSize);
        Slice<User> foundUsers = userRepository.findBySimilarUsername(username, request);
        List<UsernameSearchResultDTO> searchResultDTOS =
                foundUsers.stream()
                        .map(user ->
                                new UsernameSearchResultDTO(user.getId(), user.getForumUsername(),
                                        friendRelationshipRepository.findByTwoUsers(searchingUser,
                                                user).isPresent())).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS, foundUsers.getNumber(), foundUsers.hasNext());
    }
}
