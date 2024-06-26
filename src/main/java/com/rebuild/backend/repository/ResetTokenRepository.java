package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.tokens.ResetPasswordToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetTokenRepository extends CrudRepository<ResetPasswordToken, UUID> {

    Optional<ResetPasswordToken> findByActualToken(String token);

}
