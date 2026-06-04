package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.CommentFetchDTO;
import com.rebuild.backend.utils.exceptions.ApiException;
import com.rebuild.backend.model.responses.LoadCommentsResponse;
import com.rebuild.backend.model.entities.forum_entities.Comment;
import com.rebuild.backend.model.entities.forum_entities.ForumPost;
import com.rebuild.backend.model.entities.forum_entities.Like;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.forms.forum_forms.CommentForm;
import com.rebuild.backend.repository.forum_repositories.CommentRepository;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.LikeRepository;
import com.rebuild.backend.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentsService {

    private final CommentRepository commentRepository;

    private final ForumPostRepository postRepository;

    private final LikeRepository likeRepository;

    @Autowired
    public CommentsService(CommentRepository commentRepository, ForumPostRepository postRepository, LikeRepository likeRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public void deleteComment(UUID commentID, User deletingUser){
        Comment commentToDelete = commentRepository.findByIdAndAuthor(commentID, deletingUser).orElseThrow(
                () -> new BelongingException("This comment does not belong to you")
        );
        ForumPost associatedPost = commentToDelete.getAssociatedPost();
        associatedPost.setCommentCount(associatedPost.getCommentCount() - 1);

        if(commentToDelete.getParentId() != null){
            Comment parentComment = commentRepository.findById(commentToDelete.getParentId()).orElseThrow(
                    () -> new NotFoundException("Parent comment with this id not found")
            );
            parentComment.setRepliesCount(parentComment.getRepliesCount() - 1);
        }
        //Comments are "soft-deleted"
        commentToDelete.setDeleted(true);
        commentRepository.save(commentToDelete);
    }


    private String determineCommentDisplayName(Comment createdComment, User creatingUser, ForumPost associatedPost)
    {
        if (createdComment.isAnonymized())
        {
            return StringUtil.getAnonymizedName(creatingUser.getAnonymizedNameBase(),
                    associatedPost.getId());
        }
        return creatingUser.getForumUsername();
    }

    private void configureComment(Comment comment, ForumPost associatedPost, User user,
                                  UUID commentParentId, boolean isAnonymized){
        comment.setAnonymized(isAnonymized);
        associatedPost.getComments().add(comment);
        associatedPost.setCommentCount(associatedPost.getCommentCount() + 1);
        comment.setAssociatedPost(associatedPost);
        comment.setParentId(commentParentId);
        user.getMadeComments().add(comment);
        comment.setUser(user);
    }

    @Transactional
    public CommentDisplayDTO createComment(CommentForm commentForm, UUID parentId, User creatingUser)
    {
        //Try to find a post that corresponds to this id. If we find one, create a top-level comment
        Optional<ForumPost> foundPost = postRepository.findByIdWithComments(parentId);


        if (foundPost.isPresent())
        {
            return makeTopLevelComment(commentForm, foundPost.get(), creatingUser);
        }

        //Otherwise, try to find a comment that corresponds to this id. If we find one, create a reply to it.
        //If we can't find one, throw an exception.
        else
        {
            Comment parentComment = commentRepository.findById(parentId).
                    orElseThrow(() -> new NotFoundException("Comment or post with the specified id not found"));

            return createReplyTo(parentComment, creatingUser, commentForm);
        }

    }


    private CommentDisplayDTO makeTopLevelComment(CommentForm commentForm, ForumPost post, User creatingUser){
        Comment newComment = new Comment(commentForm.content());
        configureComment(newComment, post, creatingUser, post.getId(), commentForm.remainAnonymous());

        Comment savedComment =  commentRepository.save(newComment);
        return new CommentDisplayDTO(savedComment.getId(), savedComment.getContent(),
                determineCommentDisplayName(savedComment, creatingUser, post), 0, post.getUser().equals(creatingUser),
                false, false);

    }


    private CommentDisplayDTO createReplyTo(Comment parentComment, User creatingUser,
                                 CommentForm commentForm){

        if (parentComment.isDeleted())
        {
            throw new ApiException(HttpStatus.CONFLICT, "This comment has been deleted, you can't reply to deleted comments");
        }

        ForumPost associatedPost = parentComment.getAssociatedPost();
        Comment newComment = new Comment(commentForm.content());
        configureComment(newComment, associatedPost, creatingUser, parentComment.getId(), commentForm.remainAnonymous());

        parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);

        Comment savedComment = commentRepository.save(newComment);
        return new CommentDisplayDTO(savedComment.getId(), savedComment.getContent(),
                determineCommentDisplayName(savedComment, creatingUser, associatedPost), 0,
                associatedPost.getUser().equals(creatingUser), false, false);
    }

    public LoadCommentsResponse obtainMoreComments(UUID postId, UUID parentCommentId, User user,
                                                   int pageNumber)
    {
        if (parentCommentId == null)
        {
            return loadMoreTopLevelComments(postId, user, pageNumber);
        }
        else return loadRepliesToComment(postId, parentCommentId, user, pageNumber);
    }

    private LoadCommentsResponse loadMoreTopLevelComments(UUID postId, User user, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }
        ForumPost foundPost = postRepository.findByIdWithUser(postId).orElseThrow(() ->
                new NotFoundException("Post with this id is not found"));
        Pageable request = PageRequest.of(pageNumber, 20);

        Slice<CommentFetchDTO> loadedComments = commentRepository.loadAdditionalComments(postId,
                user.getId(), request);

        List<CommentDisplayDTO> displayedList = loadedComments.stream().map(commentFetchDTO ->
                commentFetchDTO.toDisplayDto(commentFetchDTO.authorId().equals(foundPost.getUser().getId()))).
                toList();

        return new LoadCommentsResponse(displayedList, loadedComments.getNumber(),
                loadedComments.hasNext());
    }

    private LoadCommentsResponse loadRepliesToComment(UUID postId, UUID parent_id, User user, int pageNumber)
    {
        if (pageNumber < 0)
        {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page number must be greater than or equal to zero");
        }
        ForumPost foundPost = postRepository.findByIdWithUser(postId).orElseThrow(() ->
                new NotFoundException("Post with this id is not found"));
        Pageable request = PageRequest.of(pageNumber, 20);
        Slice<CommentFetchDTO> fetchedList = commentRepository.
                loadParentCommentExpansion(parent_id, user.getId(), request);

        List<CommentDisplayDTO> displayList = fetchedList.stream().map(commentFetchDTO ->
                        commentFetchDTO.toDisplayDto(commentFetchDTO.authorId().equals(foundPost.getUser().getId()))).
                toList();

        return new LoadCommentsResponse(displayList, fetchedList.getNumber(),
                fetchedList.hasNext());

    }


    public Comment likeComment(UUID comment_id, User likingUser)
    {
        Comment comment = commentRepository.findById(comment_id).orElseThrow(
                () -> new NotFoundException("Comment with this id is not found"));

        Optional<Like> foundLike = likeRepository.findByLikedObjectIdAndLikingUserId(comment_id,
                likingUser.getId());

        //If the user has already liked this comment, remove the like.
        foundLike.ifPresent(like -> {
            likeRepository.delete(like);
            comment.setLikeCount(comment.getLikeCount() - 1);
            
        });

        //If the user has not liked this comment, simply add a like for this comment for this user.

        Like newLike = new Like(likingUser.getId(), comment_id);

        likeRepository.save(newLike);
        comment.setLikeCount(comment.getLikeCount() + 1);

        return commentRepository.save(comment);
    }
}
