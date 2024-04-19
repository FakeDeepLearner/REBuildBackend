package com.rebuild.backend.service;

import com.rebuild.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository repository;
}
