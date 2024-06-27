package com.rebuild.backend.model.entities;


public enum TokenTypes {
    ACTIVATE_ACCOUNT("account_activation"),

    CHANGE_PASSWORD("password_change"),

    CHANGE_EMAIL("email_change");

    private final String typeName;
    TokenTypes(String typeName){
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
