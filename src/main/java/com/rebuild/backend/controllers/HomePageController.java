package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.forum_entities.PostSearchConfiguration;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.resume_entities.ResumeSearchConfiguration;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.forum_forms.ForumSpecsForm;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.model.responses.ForumPostPageResponse;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.repository.ResumeSearchRepository;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
public class HomePageController {

    private final UserService userService;

    private final ResumeService resumeService;

    private final ResumeSearchRepository searchRepository;

    @Autowired
    public HomePageController(UserService userService, ResumeService resumeService, ResumeSearchRepository searchRepository) {
        this.userService = userService;
        this.resumeService = resumeService;
        this.searchRepository = searchRepository;
    }

    @PostMapping("/get_posts/configuration/{config_id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getPosts(@RequestParam(name = "token", required = false) String searchToken,
                                      @AuthenticationPrincipal User user,
                                      @PathVariable UUID config_id) {
        try {

            ResumeSearchConfiguration foundConfig = searchRepository.findById(config_id).get();

            
            ResumeSpecsForm craftedBody = resumeService.createSpecsForm(foundConfig);

            HomePageData response = userService.getSearchResult(craftedBody, null, user,
                            0, 20);

            return ResponseEntity.ok(response);
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping( "home/create_resume_search_config")
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeSearchConfiguration createSearchConfig(@AuthenticationPrincipal User authenticatedUser,
                                                        @RequestBody ResumeSpecsForm specsForm)
    {
        return resumeService.createSearchConfig(authenticatedUser, specsForm);
    }

    @GetMapping("/home/resume/{index}")
    @ResponseStatus(HttpStatus.OK)
    public Resume getResume(@AuthenticationPrincipal User user,
                            @PathVariable int index){
        return resumeService.findByUserIndex(user, index);
    }

    @PostMapping("/home/search")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData loadHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                     @RequestBody ResumeSpecsForm specsForm,
                                     @RequestParam(name = "token", required = false) String searchToken) {
        return userService.getSearchResult(specsForm, searchToken,
                authenticatedUser, pageNumber, pageSize);
    }

    @GetMapping("/home")
    @ResponseStatus(HttpStatus.OK)
    public HomePageData loadHomePage(@AuthenticationPrincipal User authenticatedUser,
                                     @RequestParam(defaultValue = "0", name = "page") int pageNumber,
                                     @RequestParam(defaultValue = "10", name = "size") int pageSize,
                                     @RequestParam(name = "token", required = false) String searchToken) {
        return userService.getHomePageData(authenticatedUser, pageNumber, pageSize, searchToken);
    }

    @PostMapping("/api/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createNewResume(@RequestBody String name,
                                          @AuthenticationPrincipal User authenticatedUser) {
        try{
            Resume createdResume = resumeService.createNewResumeFor(name, authenticatedUser);

            return ResponseEntity.status(CREATED).body(createdResume);
        }
        catch (DataIntegrityViolationException e){
            Throwable cause = e.getCause();
            if (cause instanceof ConstraintViolationException violationException &&
                    Objects.equals(violationException.getConstraintName(), "uk_same_user_resume_name")) {
                return ResponseEntity.status(CONFLICT).body("You already have a resume with this name");
            }
        }
        catch (RuntimeException e) {
            return ResponseEntity.status(PAYMENT_REQUIRED).body(e.getMessage());
        }

        //Should never get here.
        return null;
    }

    @DeleteMapping("/api/delete/{res_id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteResume(@PathVariable UUID res_id){
        resumeService.deleteById(res_id);
    }


    @DeleteMapping("/api/delete_phone")
    @ResponseStatus(NO_CONTENT)
    public void removePhoneNumber(@AuthenticationPrincipal User authenticatedUser) {

        userService.removePhoneOf(authenticatedUser);
    }

    @PostMapping("/api/update_time_zone")
    @ResponseStatus(OK)
    public User updateTimeZone(@AuthenticationPrincipal User updatingUser,
                               @RequestBody String timeZone){
        return userService.modifyTimeZone(updatingUser, timeZone);
    }

}
