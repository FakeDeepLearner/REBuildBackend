package com.rebuild.backend.exceptions;

public class ServerError extends RuntimeException {
    public ServerError() {
        super("An unexpected error occurred.");
    }
}
