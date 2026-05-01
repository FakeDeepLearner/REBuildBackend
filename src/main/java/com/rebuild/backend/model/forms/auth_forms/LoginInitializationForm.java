package com.rebuild.backend.model.forms.auth_forms;

public record LoginInitializationForm(String emailOrPhone,
                                      String password) {
}
