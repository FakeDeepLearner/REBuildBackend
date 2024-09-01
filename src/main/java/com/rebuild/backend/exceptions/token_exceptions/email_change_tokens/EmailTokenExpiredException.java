package com.rebuild.backend.exceptions.token_exceptions.email_change_tokens;

public non-sealed class EmailTokenExpiredException extends EmailChangeTokenException {
    private final String oldMail;

    private final String newMail;

    public EmailTokenExpiredException(String message,
                                      String oldMail,
                                      String newMail) {
        super(message);
        this.oldMail = oldMail;
        this.newMail = newMail;
    }

    public String getNewMail() {
        return newMail;
    }

    public String getOldMail() {
        return oldMail;
    }
}
