package com.rebuild.backend.model.entities.enums;


public enum TokenType {
    ACTIVATE_ACCOUNT("account_activation"),

    CHANGE_PASSWORD("password_change"),

    CHANGE_EMAIL("email_change");

    public final String typeName;
    TokenType(String typeName){
        this.typeName = typeName;
    }
}
