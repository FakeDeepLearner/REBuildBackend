package com.rebuild.backend.controllers.resume_controllers;


import com.rebuild.backend.repository.ResumeRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/delete")
@ResponseStatus(HttpStatus.NO_CONTENT)
public class ResumeDeleteController {

    private final ResumeRepository repository;

    @Autowired
    public ResumeDeleteController(ResumeRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/header/{id}")
    public void deleteHeader(@PathVariable UUID id){
        repository.deleteHeader(id);

    }

    @DeleteMapping("/experience/{id}")
    public void deleteExperience(@PathVariable UUID id){
        repository.deleteExperience(id);
    }

    @DeleteMapping("/education/{id}")
    public void deleteEducation(@PathVariable UUID id){
        repository.deleteEducation(id);
    }
}
