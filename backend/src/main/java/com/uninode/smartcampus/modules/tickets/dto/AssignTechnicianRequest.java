package com.uninode.smartcampus.modules.tickets.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignTechnicianRequest {

    @NotNull(message = "assignedUserId: Technician user ID is required")
    private Long assignedUserId;
}
