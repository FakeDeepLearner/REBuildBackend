package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.forum_entities.CommentReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentReplyRepository extends JpaRepository<CommentReply, UUID> {


    Optional<CommentReply> findCommentReplyByTopLevelCommentId(UUID top_level_comment_id);


    Optional<CommentReply> findByParentReplyId(UUID parent_reply_id);
}
