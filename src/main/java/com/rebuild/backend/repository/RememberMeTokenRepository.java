package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.users.RememberMeToken;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RememberMeTokenRepository extends JpaRepository<RememberMeToken, UUID> {

    List<RememberMeToken> findByExpiryTimeBefore(@NonNull LocalDateTime expiryTimeBefore);


}
