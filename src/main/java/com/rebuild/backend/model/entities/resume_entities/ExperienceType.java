package com.rebuild.backend.model.entities.resume_entities;

import lombok.Getter;

@Getter
public enum ExperienceType {

    FULL_TIME("Full Time"),

    PART_TIME("Part Time"),

    CASUAL("Casual"),

    VOLUNTEER("Volunteer"),

    INTERNSHIP("Internship"),

    CONTRACT("Contract"),

    FREELANCE("Freelance"),

    SELF_EMPLOYED("Self-Employed");

    public final String storedValue;

    ExperienceType(String storedValue) {
        this.storedValue = storedValue;
    }

    public static ExperienceType fromValue(String value)
    {
        for (ExperienceType type : values()) {
            if (type.storedValue.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with storedValue = " + value);
    }

    @Override
    public String toString() {
        return storedValue;
    }
}
