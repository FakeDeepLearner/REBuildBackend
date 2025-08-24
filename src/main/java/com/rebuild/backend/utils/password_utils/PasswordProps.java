package com.rebuild.backend.utils.password_utils;

import org.springframework.stereotype.Component;


public enum PasswordProps
{
    MIN_SIZE(8),

    MIN_UPPERCASE(1),

    MIN_LOWERCASE(1),

    MIN_SPECIAL_CHARACTER(1),

    MIN_DIGIT(1),

    CAN_HAVE_SPACES(false);

    public final int value;

    PasswordProps(int value)
    {
        this.value = value;
    }

    PasswordProps(boolean value)
    {
        this.value = value ? 1 : 0;
    }
}
