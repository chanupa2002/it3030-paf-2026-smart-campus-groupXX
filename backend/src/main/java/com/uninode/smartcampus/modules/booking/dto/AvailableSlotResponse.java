package com.uninode.smartcampus.modules.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AvailableSlotResponse(
        @JsonProperty("slot_id")
        Long slotId,
        @JsonProperty("slot")
        Long slot) {
}
