package com.rebuild.backend.utils.database_utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserContext {

    private static final ThreadLocal<UUID> userID = new ThreadLocal<>();

    public static void set(UUID userId) {
        userID.set(userId);
    }

    public static UUID get() {
        return userID.get();
    }

    public static void clear() {
        userID.remove();
    }



}
