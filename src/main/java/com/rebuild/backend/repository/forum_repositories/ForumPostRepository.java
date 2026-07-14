package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.CommentFetchDTO;
import com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.ForumPostSummaryDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    @Query(value = """
            SELECT fp FROM ForumPost fp
            LEFT JOIN FETCH fp.resumes
            JOIN fp.user u WHERE fp.id=?1
           """)
    Optional<ForumPost> findByIdWithMoreInfo(UUID id);

    Optional<ForumPost> findByIdAndUser(UUID id, User user);

    @Query(value = """
            SELECT fp FROM ForumPost fp
            LEFT JOIN FETCH fp.comments
            WHERE fp.id=?1
           """)
    Optional<ForumPost> findByIdWithComments(UUID id);

    @Query(value = """
            SELECT NEW com.rebuild.backend.model.dtos.forum_dtos.comment_and_post_dtos.CommentFetchDTO(
            u.id, p.id, c.id, c.content, u.forumUsername, c.repliesCount,
            c.isDeleted, c.createdAt,
            c.lastModifiedAt, c.isDeleted)
            FROM ForumPost p LEFT JOIN p.comments c JOIN c.user u
            WHERE p.id=?1 AND c.parentId=?1 ORDER BY c.createdAt ASC
           """)
    Slice<CommentFetchDTO> loadCommentsById(UUID id, UUID userId, Pageable pageable);

    @Query(
    value = """
    SELECT
    fp.id, fp.title, fp.content, fp.commentCount
    FROM posts fp WHERE
    (?1 IS NULL OR to_tsvector('english', fp.title) @@ websearch_to_tsquery('english', ?1))
    AND (?2 IS NULL OR to_tsvector('english', fp.content) @@ websearch_to_tsquery('english', ?2))
    ORDER BY fp.createdAt DESC, fp.lastModifiedAt DESC
    """, nativeQuery = true)
    Slice<ForumPostSummaryDTO> findByTitleAndContent(String title, String content, UUID userID, Pageable pageable);


    @Query("""
    SELECT fp FROM ForumPost fp
    WHERE fp.user=?1
    ORDER BY fp.createdAt DESC
    """)
    List<ForumPost> findByUserOrdered(User user);

    @Query(value = """
    SELECT fp FROM ForumPost fp
    JOIN FETCH fp.user
    WHERE fp.id=?1
    """)
    Optional<ForumPost> findByIdWithUser(UUID id);
}
