package com.rebuild.backend.model.entities.enums;

public enum TokenBlacklistPurpose {
    EMAIL_CHANGE("change_email"),

    PASSWORD_CHANGE("change_password"),

    ACCOUNT_ACTIVATION("activate_account");

    public final String purposeName;


    TokenBlacklistPurpose(String purposeName) {
        this.purposeName = purposeName;
    }
}
