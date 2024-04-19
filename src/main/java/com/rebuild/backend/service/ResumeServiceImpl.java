package com.rebuild.backend.service;


import com.rebuild.backend.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class ResumeServiceImpl implements ResumeService {

    @Autowired
    private ResumeRepository repository;

}
