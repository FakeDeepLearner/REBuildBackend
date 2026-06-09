package com.rebuild.backend.model.forms.auth_forms;

public record LoginFinalizationForm(String email,
                                    String password,
                                    String enteredCode) implements EmailAndPasswordForm{

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
