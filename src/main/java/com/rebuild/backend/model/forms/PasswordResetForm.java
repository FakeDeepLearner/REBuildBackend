package com.rebuild.backend.model.forms;

import jakarta.validation.constraints.Email;

public record PasswordResetForm(@Email
                                  String enteredEmail) {
}
