package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.dtos.forum_dtos.CommentFetchDTO;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.user_entities.User;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<@NonNull Comment, @NonNull UUID> {

    @Query(
        """
        SELECT new com.rebuild.backend.model.dtos.forum_dtos.CommentFetchDTO(
          u.id, p.id, c.id, c.content, u.forumUsername, c.repliesCount,
          CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.likedObjectId=c.id AND l.likingUserId=?2) > 0
          THEN true ELSE false END, c.isDeleted, c.isAnonymized, u.anonymizedNameBase)
          FROM Comment c JOIN c.user u JOIN c.associatedPost p WHERE c.parentId=?1
          ORDER BY c.creationDate ASC"""
    )
    List<CommentFetchDTO> loadParentCommentExpansion(UUID parentId, UUID userId);


    @Query(
            """
            SELECT new com.rebuild.backend.model.dtos.forum_dtos.CommentFetchDTO(
              u.id, p.id, c.id, c.content, u.forumUsername, c.repliesCount,
              CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.likedObjectId=c.id AND l.likingUserId=?2) > 0
              THEN true ELSE false END, c.isDeleted, c.isAnonymized, u.anonymizedNameBase)\s
              FROM Comment c JOIN c.user u JOIN c.associatedPost p WHERE p=?1
              ORDER BY c.creationDate ASC"""
    )
    Slice<CommentFetchDTO> loadAdditionalComments(ForumPost post, UUID userId, Pageable pageable);


    @Query("""
    SELECT c FROM Comment c
    WHERE c.id=?1 AND c.user=?2
    """)
    Optional<Comment> findByIdAndAuthor(UUID id, User author);

    @Query("""
    SELECT c FROM Comment c
    WHERE c.user=?1 AND c.isAnonymized=false
    """)
    List<Comment> findByUserUnAnonymized(User user);
}
