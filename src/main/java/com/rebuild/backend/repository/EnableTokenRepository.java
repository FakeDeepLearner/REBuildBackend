package com.rebuild.backend.repository;

import com.rebuild.backend.model.entities.EnableAccountToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnableTokenRepository extends CrudRepository<EnableAccountToken, UUID> {

    Optional<EnableAccountToken> findByEmailFor(String email);

    Optional<EnableAccountToken> findByToken(String token);
}
