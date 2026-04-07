package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.dtos.forum_dtos.ForumPostSummaryDTO;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.profile_entities.UserProfile;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import lombok.NonNull;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    @Query(value = """
            SELECT fp FROM ForumPost fp
            LEFT JOIN FETCH fp.resumes
            LEFT JOIN FETCH fp.uploadedFiles
                        JOIN fp.associatedProfile.user u WHERE fp.id=?1
           """)
    Optional<ForumPost> findByIdWithMoreInfo(UUID id);

    @Query(value = """
            SELECT fp FROM ForumPost fp
            LEFT JOIN FETCH fp.comments
            WHERE fp.id=?1
           """)
    Optional<ForumPost> findByIdWithComments(UUID id);

    @Query(value = """
            SELECT NEW com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO(
            c.id, c.content, COALESCE(u.forumUsername, u.backupForumUsername), c.repliesCount,
            CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.likedObjectId=?1 AND l.likingUserId=?2) > 0
            THEN true ELSE false END)
            FROM ForumPost p LEFT JOIN p.comments c JOIN c.associatedProfile.user u
            WHERE p.id=?1 ORDER BY c.creationDate ASC
           \s""")
    Slice<CommentDisplayDTO> loadCommentsById(UUID id, UUID userId, Pageable pageable);

    @Query(value = """
            SELECT fp FROM ForumPost fp
            LEFT JOIN FETCH fp.uploadedFiles
            JOIN fp.associatedProfile.user u WHERE fp.id=?1 AND u=?2
           """)
    Optional<ForumPost> findByIdWithFiles(UUID id, User creatingUser);

    @Query(
    value = """
    SELECT NEW com.rebuild.backend.model.dtos.forum_dtos.ForumPostSummaryDTO(
    fp.id, fp.title, fp.content, fp.likeCount, fp.commentCount,
    CASE WHEN (SELECT COUNT(l) FROM Like l WHERE l.likedObjectId=fp.id AND l.likingUserId=?3) > 0
    THEN true ELSE false END)
    FROM ForumPost fp WHERE
    (?1 IS NULL OR fp.title LIKE CONCAT('%', ?1, '%'))
    AND (?2 IS NULL OR fp.content LIKE CONCAT('%', ?2, '%'))
    ORDER BY fp.creationDate DESC, fp.lastModificationDate DESC
    """)
    Slice<ForumPostSummaryDTO> findByTitleAndContent(String title, String content, UUID userID, Pageable pageable);
}
