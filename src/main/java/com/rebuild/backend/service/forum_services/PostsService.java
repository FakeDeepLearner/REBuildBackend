package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.model.dtos.forum_dtos.CommentDisplayDTO;
import com.rebuild.backend.model.dtos.forum_dtos.CommentFetchDTO;
import com.rebuild.backend.model.dtos.forum_dtos.PostDisplayDTO;
import com.rebuild.backend.model.entities.forum_entities.*;
import com.rebuild.backend.model.entities.user_entities.User;
import com.rebuild.backend.model.forms.forum_forms.EditPostForm;
import com.rebuild.backend.model.responses.forum_responses.EditPostResponse;
import com.rebuild.backend.model.responses.resume_responses.ResumePreviewResponse;
import com.rebuild.backend.utils.exceptions.BelongingException;
import com.rebuild.backend.utils.exceptions.FileUploadException;
import com.rebuild.backend.utils.exceptions.NotFoundException;
import com.rebuild.backend.model.forms.forum_forms.NewPostForm;
import com.rebuild.backend.repository.forum_repositories.ForumPostRepository;
import com.rebuild.backend.repository.forum_repositories.LikeRepository;
import com.rebuild.backend.repository.resume_repositories.ResumeRepository;
import com.rebuild.backend.utils.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PostsService {

    private final ForumPostRepository postRepository;

    private final ResumeRepository resumeRepository;

    private final LikeRepository likeRepository;


    @Autowired
    public PostsService(ResumeRepository resumeRepository,
                        ForumPostRepository postRepository,
                        LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.resumeRepository = resumeRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public ForumPost createNewPost(NewPostForm postForm,
                                                  User creatingUser) {
        ForumPost newPost = new ForumPost(postForm.title(), postForm.content());
        newPost.setAnonymized(postForm.remainAnonymous());
        List<PostResume> resumes = resumeRepository.findByUserAndIdIn(creatingUser, postForm.resumeIDs()).stream()
                        .map(PostResume::new).
                        peek(postResume -> postResume.setAssociatedPost(newPost)).
                toList();
        newPost.setResumes(resumes);

        newPost.setUser(creatingUser);
        creatingUser.getMadePosts().add(newPost);
        return postRepository.save(newPost);
    }


    @Transactional
    public void deletePost(UUID postID, User deletingUser){
        ForumPost postToDelete = postRepository.findByIdAndUser(postID, deletingUser).
                orElseThrow(() -> new BelongingException("This post does not belong to you, so you can't delete it"));


        postRepository.delete(postToDelete);
    }

    @Transactional
    public String changePostAnonymization(UUID postID, User anonymizingUser){
        ForumPost postToAnonymize = postRepository.findByIdAndUser(postID, anonymizingUser).
                orElseThrow(() -> new BelongingException("This post does not belong to you, so you can't delete it"));

        postToAnonymize.setAnonymized(!postToAnonymize.isAnonymized());
        ForumPost savedPost = postRepository.save(postToAnonymize);
        return StringUtil.determineDisplayedCommentName(savedPost.isAnonymized(),
                anonymizingUser.getForumUsername(), anonymizingUser.getAnonymizedNameBase(), savedPost.getId());
    }

    public PostDisplayDTO loadPost(UUID postID, User loadingUser, int pageSize){
        ForumPost forumPost = postRepository.findByIdWithMoreInfo(postID).orElseThrow(
                () -> new NotFoundException("Post with this id is not found")
        );
        User postUser = forumPost.getUser();

        //When we are loading a post, we just fetch the initial page of the comments
        Pageable request = PageRequest.of(0, pageSize);
        Slice<CommentFetchDTO> fetchedComments =
                postRepository.loadCommentsById(postID, loadingUser.getId(), request);


        List<CommentDisplayDTO> displayedComments = fetchedComments.stream().map(commentFetchDTO ->
                commentFetchDTO.toDisplayDto(commentFetchDTO.authorId().equals(postUser.getId()))).
                toList();


        String displayedName = StringUtil.determineDisplayedCommentName(forumPost.isAnonymized(),
                postUser.getForumUsername(), postUser.getAnonymizedNameBase(), forumPost.getId());
        List<ResumePreviewResponse> previews = forumPost.getResumes().stream().map(
                postResume -> new ResumePreviewResponse(postResume.getId(),
                        null, postResume.getPreviewUrl())
        ).toList();

        Instant postedTime = forumPost.isEdited() ? forumPost.getLastModifiedAt() : forumPost.getCreatedAt();
        boolean userHasLikedPost = likeRepository.findByLikedObjectIdAndLikingUserId(postID,
                loadingUser.getId()).isPresent();
        return new PostDisplayDTO(forumPost.getId(), forumPost.getTitle(), forumPost.getContent(),
                displayedName, postedTime, previews, displayedComments,
                fetchedComments.getNumber(), fetchedComments.hasNext(),
                userHasLikedPost);

    }




    public ForumPost likePost(UUID post_id, User likingUser)
    {
        ForumPost post = postRepository.findById(post_id).orElseThrow(
                () -> new NotFoundException("Post with this id is not found"));

        Optional<Like> foundLike = likeRepository.findByLikedObjectIdAndLikingUserId(post_id,
                likingUser.getId());

        //If the user has already liked this post, remove the like.
        foundLike.ifPresent(like -> {
            likeRepository.delete(like);
            post.setLikeCount(post.getLikeCount() - 1);
        });

        //If the user has not liked this post, simply add a like for this post for this user.

        Like newLike = new Like(likingUser.getId(), post_id);

        likeRepository.save(newLike);
        post.setLikeCount(post.getLikeCount() + 1);

        return postRepository.save(post);
    }

    public EditPostResponse editPost(UUID postId, User editingUser, EditPostForm editPostForm)
    {
        ForumPost postToEdit = postRepository.findByIdAndUser(postId, editingUser).
                orElseThrow(() -> new BelongingException("This post does not belong to you, so you can't delete it"));

        postToEdit.setContent(editPostForm.newContent());
        postToEdit.setTitle(editPostForm.newTitle());

        postToEdit.setEdited(true);

        ForumPost savedPost = postRepository.save(postToEdit);
        return new EditPostResponse(savedPost.getTitle(), savedPost.getContent(), savedPost.getLastModifiedAt());
    }

}
