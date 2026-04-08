package com.uninode.smartcampus.modules.tickets.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    REJECTED;

    @JsonCreator
    public static TicketStatus fromString(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.equalsIgnoreCase("pending")) {
            return OPEN; // treat 'pending' as OPEN
        }
        try {
            return TicketStatus.valueOf(v.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
