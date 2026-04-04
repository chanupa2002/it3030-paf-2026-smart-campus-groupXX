package com.uninode.smartcampus.modules.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long attachmentId;
    private Long ticketId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Integer index;
    private LocalDateTime createdAt;
    private UserInfo uploadedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String name;
    }
}
