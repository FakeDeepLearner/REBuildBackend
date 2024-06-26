package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.tokens.EmailChangeToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailChangeTokenRepository extends CrudRepository<EmailChangeToken, UUID> {

    Optional<EmailChangeToken> findByToken(String token);

    Optional<EmailChangeToken> findByEmailFor(String email);
}
