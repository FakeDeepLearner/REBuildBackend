package com.rebuild.backend.controllers;

import com.rebuild.backend.exceptions.not_found_exceptions.UserNotFoundException;
import com.rebuild.backend.exceptions.resume_exceptions.MaxResumesReachedException;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.service.ResumeService;
import com.rebuild.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
public class HomePageController {

    private final UserService userService;

    private final ResumeService resumeService;

    @Autowired
    public HomePageController(UserService userService, ResumeService resumeService) {
        this.userService = userService;
        this.resumeService = resumeService;
    }


    @GetMapping("/home/{user_id}")
    @ResponseStatus(HttpStatus.OK)
    public List<Resume> getAllResumes(@PathVariable UUID user_id){
        return userService.getAllResumesById(user_id);
    }

    @PostMapping("/api/{user_id}/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Resume createNewResume(@PathVariable UUID user_id){
        User creatingUser = userService.findByID(user_id).
                orElseThrow(() -> new UserNotFoundException("User not found with the given id"));
        return resumeService.createNewResumeFor(creatingUser);
    }

    @DeleteMapping("/api/delete/{res_id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResume(@PathVariable UUID res_id){
        resumeService.deleteById(res_id);
    }

    @GetMapping("/api/download/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<InputStreamResource> downloadResumeAsPdf(@PathVariable UUID id){
        Resume fetchedResume = resumeService.findById(id);

        byte[] pdfAsBytes = resumeService.returnResumeAsPdf(fetchedResume);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfAsBytes);
        InputStreamResource body = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=resume.pdf");

        return ResponseEntity.ok().
                contentType(MediaType.APPLICATION_PDF).
                headers(headers).
                body(body);

    }

}
