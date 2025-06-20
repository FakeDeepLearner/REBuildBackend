package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.enums.LikeType;
import com.rebuild.backend.model.entities.forum_entities.Like;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikeRepository extends JpaRepository<Like, UUID> {
}
