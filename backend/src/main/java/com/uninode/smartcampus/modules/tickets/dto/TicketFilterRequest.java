package com.uninode.smartcampus.modules.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.uninode.smartcampus.modules.tickets.entity.Priority;
import com.uninode.smartcampus.modules.tickets.entity.TicketStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketFilterRequest {

    private TicketStatus status;
    private Priority priority;
    private Long userId;
    private Long resourceId;
    private Integer pageNumber;
    private Integer pageSize;
    private String sortBy;
    private String sortOrder;
}
