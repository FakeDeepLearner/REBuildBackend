package com.rebuild.backend.model.exceptions;

public class FileUploadException extends RuntimeException {
    public FileUploadException(int index, String message, Throwable cause) {
        super(message, cause);
    }
}
