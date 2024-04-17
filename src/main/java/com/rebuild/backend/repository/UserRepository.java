package com.rebuild.backend.repository;

import com.rebuild.backend.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface UserRepository extends CrudRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE User u SET u.password=:newHashedPassword WHERE u.id=:userID")
    void changePassword(UUID userID, String newHashedPassword);

    @Modifying(flushAutomatically = true)
    @Query("UPDATE User u SET u.email=:newEmail WHERE u.id=:userID")
    void changeEmail(UUID userID, String newEmail);

    @Query("SELECT r.id FROM Resume r WHERE r.user.id=:userID")
    List<UUID> getAllResumesById(UUID userID);



}
