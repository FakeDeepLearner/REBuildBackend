package com.rebuild.backend.model.forms.dtos.error_dtos;

import com.rebuild.backend.model.entities.users.User;

import java.util.Optional;

public record CreateUserResult(Optional<User> optionalUser, Optional<String> optionalError) {
}
