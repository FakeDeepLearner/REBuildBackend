package com.rebuild.backend.utils.specs;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.users.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ForumPostSpecifications {

    public static Specification<ForumPost> isPostedBy(String forumUsername) {
        return (root, query, builder) -> {
            Join<ForumPost, User> postUserJoin = root.join("creatingUser");
            return builder.equal(postUserJoin.get("forumUsername"), forumUsername);
        };
    }

    public static Specification<ForumPost> isPostedAfter(LocalDateTime cutoff) {
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("creationDate").as(LocalDateTime.class), cutoff);
    }

    public static Specification<ForumPost> isPostedBefore(LocalDateTime cutoff) {
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("creationDate").as(LocalDateTime.class), cutoff);
    }

    public static Specification<ForumPost> titleContains(String substring) {
        return (root, query, builder) ->
                builder.like(root.get("title"), "%" + substring + "%");
    }

    public static Specification<ForumPost> titleStartsWith(String substring) {
        return (root, query, builder) ->
                builder.like(root.get("title"), substring + "%");
    }

    public static Specification<ForumPost> bodyStartsWith(String substring) {
        return (root, query, builder) ->
                builder.like(root.get("body"), substring + "%");
    }

    public static Specification<ForumPost> titleEndsWith(String substring) {
        return (root, query, builder) ->
                 builder.like(root.get("title"), "%" + substring);
    }

    public static Specification<ForumPost> bodyEndsWith(String substring) {
        return (root, query, builder) ->
                builder.like(root.get("body"), "%" + substring );
    }

    public static Specification<ForumPost> bodyContains(String substring) {
        return (root, query, builder) ->
                builder.like(root.get("content"), "%" + substring + "%");
    }

    public static Specification<ForumPost> bodyMinSize(int minSize) {
        return (root, query, builder) ->
                builder.ge(builder.length(root.get("content")), minSize);
    }

    public static Specification<ForumPost> bodyMaxSize(int maxSize) {
        return (root, query, builder) ->
                builder.le(builder.length(root.get("content")), maxSize);
    }

}
