package com.rebuild.backend.utils.exceptions;

import org.springframework.http.HttpStatus;

public class FileUploadException extends ApiException {
    public FileUploadException(HttpStatus status, String message) {
        super(status, message);
    }
}
