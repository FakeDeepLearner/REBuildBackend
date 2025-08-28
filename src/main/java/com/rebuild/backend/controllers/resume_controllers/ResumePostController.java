package com.rebuild.backend.controllers.resume_controllers;

import com.rebuild.backend.model.entities.resume_entities.Header;
import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import com.rebuild.backend.model.forms.resume_forms.EducationForm;
import com.rebuild.backend.model.forms.resume_forms.ExperienceForm;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.resume_services.ResumeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/resume/post", method = RequestMethod.POST)
@ResponseStatus(HttpStatus.CREATED)
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ResumePostController {
    private final ResumeService resumeService;

    @Autowired
    public ResumePostController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/header/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Header createNewHeader(@Valid @RequestBody HeaderForm headerForm, @PathVariable int index,
                                  @AuthenticationPrincipal User user){
        return resumeService.createNewHeader(user, index, headerForm);
    }

    @PostMapping("/experience/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume createNewExperience(@Valid @RequestBody ExperienceForm experienceForm,
                                                 @PathVariable int index,
                                                 @AuthenticationPrincipal User user,
                                                 @RequestParam(required = false) Integer experiencesIndex){


        return resumeService.createNewExperience(user, index, experienceForm, experiencesIndex);

    }

    @PostMapping("/education/{index}")
    @CacheEvict(value = "resume_cache", key = "#user.id.toString()" + "-" + "#index")
    public Resume createNewEducation(@Valid @RequestBody EducationForm educationForm,
                                     @PathVariable int index,
                                     @AuthenticationPrincipal User user ){
        return resumeService.createNewEducation(user, index, educationForm);
    }
}
