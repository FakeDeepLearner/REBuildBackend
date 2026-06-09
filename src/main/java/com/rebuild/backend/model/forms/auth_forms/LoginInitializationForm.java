package com.rebuild.backend.model.forms.auth_forms;

public record LoginInitializationForm(String email,
                                      String password) implements EmailAndPasswordForm {
    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
