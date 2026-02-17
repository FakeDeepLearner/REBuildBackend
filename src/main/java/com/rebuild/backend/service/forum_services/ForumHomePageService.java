package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.UsernameSearchResultDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.UsernameSearchResponse;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.user_repositories.UserRepository;
import com.rebuild.backend.service.util_services.ElasticSearchService;
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

    private final ElasticSearchService searchService;

    private final PostsService postsService;

    private final UserRepository userRepository;

    @Autowired
    public ForumHomePageService(ForumPostRepository postRepository,
                                ElasticSearchService searchService,
                                PostsService postsService, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.searchService = searchService;
        this.postsService = postsService;
        this.userRepository = userRepository;
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
                                                PostSearchConfiguration postSearchConfiguration)
    {
        List<UUID> matchedResults = searchService.executePostSearch(postSearchConfiguration);
        PageRequest request = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC,
                "creationDate"));


        Page<ForumPost> foundPosts = postRepository.findByIdIn(matchedResults, request);

        return new ForumPostPageResponse(foundPosts.getContent(), foundPosts.getNumber(),
                foundPosts.getTotalElements(), foundPosts.getTotalPages(), foundPosts.getSize());
    }

    public ForumPostPageResponse getPagedResult(int pageNumber, int pageSize,
                                                ForumSpecsForm forumSpecsForm, User user)
    {

        PostSearchConfiguration createdConfiguration = postsService.createSearchConfig(user, forumSpecsForm, true);
        return getPagedResult(pageNumber, pageSize, createdConfiguration);
    }

    public UsernameSearchResponse getUsernameSearchResults(String username)
    {
        List<UUID> foundIds = searchService.executeUserSearch(username);

        List<UsernameSearchResultDTO> searchResultDTOS =
                userRepository.findAllById(foundIds).stream()
                        .map(user -> new UsernameSearchResultDTO(user.getId(), user.getForumUsername())).
                        toList();
        return new UsernameSearchResponse(searchResultDTOS);
    }
}
