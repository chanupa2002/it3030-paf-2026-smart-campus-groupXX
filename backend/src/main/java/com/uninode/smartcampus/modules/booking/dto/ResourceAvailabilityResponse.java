package com.uninode.smartcampus.modules.booking.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResourceAvailabilityResponse(
                @JsonProperty("resource_id") Long resourceId,
                @JsonProperty("resource_name") String resourceName,
                @JsonProperty("free_slots") List<AvailableSlotResponse> freeSlots) {
}
