package com.rebuild.backend.controller;

import com.rebuild.backend.model.entities.Header;
import com.rebuild.backend.model.forms.resume_forms.HeaderForm;
import com.rebuild.backend.service.ResumeService;
import org.hibernate.validator.constraints.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResumeFormController {
    private final ResumeService resumeService;

    @Autowired
    public ResumeFormController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }


    @PostMapping("api/post/header/{res_id}")
    public void createNewHeader(@PathVariable UUID res_id, @RequestBody HeaderForm headerForm){
    }


}
