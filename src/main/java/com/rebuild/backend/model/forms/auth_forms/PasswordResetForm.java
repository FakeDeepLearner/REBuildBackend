package com.rebuild.backend.model.forms.auth_forms;

public record PasswordResetForm(String newPassword, String newPasswordConfirmation) {
}
