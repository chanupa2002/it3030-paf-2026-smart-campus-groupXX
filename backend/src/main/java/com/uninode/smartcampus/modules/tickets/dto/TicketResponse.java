package com.uninode.smartcampus.modules.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.uninode.smartcampus.modules.tickets.entity.Priority;
import com.uninode.smartcampus.modules.tickets.entity.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    private Long ticketId;
    private LocalDateTime createdAt;
    private UserSummary raisedUser;
    private UserSummary assignedUser;
    private Priority priority;
    private ResourceSummary resource;
    private String description;
    private String contactNumber;
    private TicketStatus status;
    private String resolutionNotes;
    private String category;
    private List<AttachmentResponse> attachments;
    private Integer commentCount;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSummary {
        private Long userId;
        private String name;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResourceSummary {
        private Long resourceId;
        private String name;
        private String type;
        private String location;
    }
}
