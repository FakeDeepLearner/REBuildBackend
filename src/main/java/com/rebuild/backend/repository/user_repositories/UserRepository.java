package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.resume_entities.Resume;
import com.rebuild.backend.model.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query(value = """
        SELECT u FROM User u
        WHERE u.email=?1 OR u.phoneNumber=?1
       """)
    Optional<User> findByEmailOrPhoneNumber(String emailOrPhone);





}
