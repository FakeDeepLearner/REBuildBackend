package com.rebuild.backend.model.forms.dtos;


public record ResetTokenExpiredDTO(String error_message, String failedEmailFor) {
}
