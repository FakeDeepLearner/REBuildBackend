package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.PostLike;
import com.rebuild.backend.model.entities.users.User;
import lombok.NonNull;
import org.hibernate.StatelessSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    Optional<PostLike> findByLikingUserAndLikedPostId(@NonNull User likingUser,
                                                      UUID likedPost_id);

}
