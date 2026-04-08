package com.uninode.smartcampus.modules.tickets.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * Represents an attachment stored in Tickets.images JSONB column.
 * This is not a JPA entity but a data structure for JSON serialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketAttachment {

    @JsonProperty("attachmentId")
    private Long attachmentId;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("filePath")
    private String filePath;

    @JsonProperty("fileSize")
    private Long fileSize;

    @JsonProperty("fileType")
    private String fileType;

    @JsonProperty("uploadedBy")
    private Long uploadedBy;

    @JsonProperty("uploadedByName")
    private String uploadedByName;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
