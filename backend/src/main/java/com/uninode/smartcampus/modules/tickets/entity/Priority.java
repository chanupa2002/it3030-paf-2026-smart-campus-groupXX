package com.uninode.smartcampus.modules.tickets.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Priority {
    LOW,
    MEDIUM,
    HIGH;

    @JsonCreator
    public static Priority fromString(String value) {
        if (value == null) return null;
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
