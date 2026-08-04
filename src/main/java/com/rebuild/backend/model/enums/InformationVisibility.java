package com.rebuild.backend.model.enums;

public enum InformationVisibility {

    EVERYONE("Visible to Everyone"),

    FRIENDS_ONLY("Visible to Friends Only"),

    NO_ONE("Visible to No One");

    public final String value;

    InformationVisibility(String value) {
        this.value = value;
    }

    public static InformationVisibility fromValue(String value) {
        for (InformationVisibility informationVisibility : values()) {
            if (informationVisibility.value.equals(value)) {
                return informationVisibility;
            }
        }
        return null;
    }
}
