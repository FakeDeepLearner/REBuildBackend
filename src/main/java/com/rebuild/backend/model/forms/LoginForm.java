package com.rebuild.backend.model.forms;

import com.rebuild.backend.model.constraints.username.LoginFieldConstraint;

public record LoginForm(@LoginFieldConstraint
                        String emailOrUsername,
                        String password) {
}
