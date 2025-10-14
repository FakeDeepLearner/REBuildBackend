package com.rebuild.backend.controllers;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.ResumeSpecsForm;
import com.rebuild.backend.model.responses.HomePageData;
import com.rebuild.backend.service.resume_services.ResumeService;
import com.rebuild.backend.service.user_services.UserService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@RestController
public class HomePageController {

    private final UserService userService;

    private final ResumeService resumeService;

    @Autowired
    public HomePageController(UserService userService, ResumeService resumeService) {
        this.userService = userService;
        this.resumeService = resumeService;
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
