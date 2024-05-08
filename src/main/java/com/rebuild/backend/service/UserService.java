package com.rebuild.backend.service;

import com.rebuild.backend.model.entities.Resume;
import com.rebuild.backend.model.entities.User;
import com.rebuild.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService{


    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository){
        this.repository = repository;
    }

    Optional<User> findByID(UUID userID){
        return repository.findById(userID);
    }


    public Optional<User> findByEmail(String email){
        return repository.findByEmail(email);
    }

    public void changePassword(UUID userID, String newHashedPassword){
        repository.changePassword(userID, newHashedPassword);
    }

    public void changeEmail(UUID userID, String newEmail){
        repository.changeEmail(userID, newEmail);
    }

    public List<Resume> getAllResumesById(UUID userID){
        return repository.getAllResumesByID(userID);
    }


}
