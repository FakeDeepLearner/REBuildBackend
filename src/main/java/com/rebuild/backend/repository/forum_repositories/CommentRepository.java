package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import lombok.NonNull;
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
        SELECT new com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO(
          c.id, c.content, COALESCE(u.forumUsername, u.backupForumUsername), c.repliesCount)\s
              FROM Comment c JOIN c.author u WHERE c.parent.id=:parentId
                 ORDER BY c.creationDate ASC"""
    )
    List<CommentDisplayDTO> loadParentCommentInfo(UUID parentId);


    Optional<Comment> findByIdAndAuthor(UUID id, User author);
}
