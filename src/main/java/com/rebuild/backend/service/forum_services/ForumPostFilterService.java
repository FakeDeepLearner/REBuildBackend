package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.HomePageData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ForumPostFilterService {

    private Collection<ForumPost> filterByPredicate(Predicate<ForumPost> predicate,
                                                    Collection<ForumPost> initialPosts) {
        return initialPosts.stream().
                filter(predicate).toList();
    }


    public ForumPostPageResponse filterOnUsername(String username,
                                                  ForumPostPageResponse initialPageData) {
        Collection<ForumPost> filteredPosts = filterByPredicate((post) -> post.getCreatingUser().
                getForumUsername().equals(username), initialPageData.displayedPosts());
        return initialPageData;
    }

}
