package com.rebuild.backend.repository.user_repositories;

import com.rebuild.backend.model.entities.user_entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    @Query(value = """
        SELECT u FROM User u JOIN FETCH u.recoveryCodes
        WHERE u.email=?1 OR u.phoneNumber=?1
       """)
    Optional<User> findByEmailOrPhoneWithRecoveryCodes(String emailOrPhone);


    @Query(value = """
    SELECT u FROM User u
    WHERE COALESCE(u.forumUsername, u.backupForumUsername) LIKE LOWER(CONCAT('%', ?1, '%'))
    """)
    Slice<User> findBySimilarUsername(String username, Pageable pageable);


    @Query(value = """
    SELECT u FROM User u
    WHERE COALESCE(u.forumUsername, u.backupForumUsername)=?1
    """)
    Optional<User> findByForumUsername(String forumUsername);





}
