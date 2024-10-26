package com.rebuild.backend.service.forum_services;

import com.rebuild.backend.exceptions.conflict_exceptions.InvalidForumCredentialsException;
import com.rebuild.backend.exceptions.conflict_exceptions.UsernameAlreadyExistsException;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.forum_forms.ForumLoginForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.repository.UserRepository;
import com.rebuild.backend.utils.password_utils.PepperedEncoder;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ForumAuthenticationService {

    private final PepperedEncoder encoder;

    private final UserRepository userRepository;

    private final ForumPostAndCommentService postAndCommentService;

    @Autowired
    public ForumAuthenticationService(PepperedEncoder encoder, UserRepository userRepository, ForumPostAndCommentService postAndCommentService) {
        this.encoder = encoder;
        this.userRepository = userRepository;
        this.postAndCommentService = postAndCommentService;
    }

    public void validateForumCredentials(User validatingUser, ForumLoginForm loginForm){
        String hashedPassword = encoder.encode(loginForm.password());
        if (!loginForm.username().equals(validatingUser.getForumUsername())){
            throw new InvalidForumCredentialsException("Invalid username, either it does not exist or it is not yours");
        }
        if (!loginForm.password().equals(hashedPassword)){
            throw new InvalidForumCredentialsException("Invalid password, please try again");
        }

    }

    public void signUserUpToForum(String forumUsername, String rawForumPassword, User userSigningUp){
        try{
            userSigningUp.setForumUsername(forumUsername);
            String encodedPassword = encoder.encode(rawForumPassword);
            userSigningUp.setPassword(encodedPassword);
            userRepository.save(userSigningUp);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException){
                String violatedConstraint = violationException.getConstraintName();
                switch (violatedConstraint){
                    case "uk_forum_username" -> throw new UsernameAlreadyExistsException("This form username already exists");
                    case null -> {}
                    default -> throw new IllegalStateException("Unexpected value: " + violatedConstraint);
                }
            }
        }

    }

}
