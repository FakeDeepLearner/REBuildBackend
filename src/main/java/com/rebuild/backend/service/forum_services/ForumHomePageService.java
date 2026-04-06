package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.UsernameSearchResponse;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.FriendRelationshipRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    public ForumPostPageResponse serveGetRequest(int pageNumber, int pageSize)
    {
        PageRequest request =
                PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<ForumPost> foundPage = postRepository.findAll(request);

        return new ForumPostPageResponse(foundPage.getContent(), foundPage.getNumber(), foundPage.getTotalElements(),
                foundPage.getTotalPages(), foundPage.getSize());
    }

    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                ForumSpecsForm forumSpecsForm)
    {
        PageRequest request = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC,
                "creationDate"));

        Page<ForumPost> foundPosts = postRepository.findByTitleAndContent(forumSpecsForm.titleContains(),
                forumSpecsForm.bodyContains(),
                request);

        return new ForumPostPageResponse(foundPosts.getContent(), foundPosts.getNumber(),
                foundPosts.getTotalElements(), foundPosts.getTotalPages(), foundPosts.getSize());
    }

    public UsernameSearchResponse getUsernameSearchResults(String username, User searchingUser)
    {
        List<UsernameSearchResultDTO> searchResultDTOS =
                userRepository.findBySimilarUsername(username).stream()
                        .map(user ->
                                new UsernameSearchResultDTO(user.getId(), user.getForumUsername(),
                                        friendRelationshipRepository.findByTwoUsers(searchingUser,
                                                user).isPresent())).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS);
    }
}
