package com.rebuild.backend.repository;

import com.rebuild.backend.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE User u SET u.password=:newHashedPassword WHERE u.id=:userID")
    void changePassword(UUID userID, String newHashedPassword);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE User u SET u.email=:newEmail WHERE u.id=:userID")
    void changeEmail(UUID userID, String newEmail);

    



}
