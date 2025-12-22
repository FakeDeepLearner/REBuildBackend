package com.rebuild.backend.repository.forum_repositories;

import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {

    @Override
    @NonNull
    @Query(value = """
            SELECT fp FROM ForumPost fp\s
                        LEFT JOIN FETCH fp.resumes\s
                        JOIN fp.creatingUser u WHERE fp.id=:uuid
           \s""")
    Optional<ForumPost> findById(@Param("uuid") UUID uuid);

    @Query(value = """
            SELECT NEW com.rebuild.backend.model.forms.dtos.forum_dtos.CommentDisplayDTO(
            c.id, c.content, COALESCE(u.forumUsername, u.backupForumUsername), c.repliesCount)
            FROM ForumPost p LEFT JOIN p.comments c JOIN c.author u
            WHERE p.id=:id ORDER BY c.creationDate ASC
            """)
    List<CommentDisplayDTO> loadCommentsById(UUID id);

    @NonNull
    Page<ForumPost> findAll(@NonNull Pageable pageable);
}
