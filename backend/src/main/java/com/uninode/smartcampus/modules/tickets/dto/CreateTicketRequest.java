package com.uninode.smartcampus.modules.tickets.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.uninode.smartcampus.modules.tickets.entity.Priority;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTicketRequest {

    @NotNull(message = "Resource ID is required")
    private Long resourceId;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @Pattern(regexp = "^[0-9\\s\\-\\+()]+$", message = "Invalid contact number format")
    private String contactNumber;

    private String category;
}
