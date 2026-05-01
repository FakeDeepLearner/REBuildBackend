package com.rebuild.backend.model.forms.auth_forms;

public record LoginFinalizationForm(String emailOrPhone,
                                    String password,
                                    String enteredCode) {
}
