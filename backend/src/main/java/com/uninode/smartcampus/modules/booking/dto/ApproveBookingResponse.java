package com.uninode.smartcampus.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApproveBookingResponse(
        @JsonProperty("booking_id")
        Long bookingId,
        @JsonProperty("message")
        String message) {
}
