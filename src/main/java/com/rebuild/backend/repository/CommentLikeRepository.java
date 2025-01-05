package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.CommentLike;
import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID>{

    Optional<CommentLike> findByLikingUserAndLikedCommentId(@NonNull User likingUser,
                                                            UUID likedCommentId);

}
