package com.uninode.smartcampus.modules.tickets.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import com.uninode.smartcampus.modules.tickets.dto.AttachmentResponse;
import com.uninode.smartcampus.modules.tickets.entity.Ticket;
import com.uninode.smartcampus.modules.tickets.exception.InvalidFileTypeException;
import com.uninode.smartcampus.modules.tickets.exception.MaxAttachmentsExceededException;
import com.uninode.smartcampus.modules.tickets.exception.TicketNotFoundException;
import com.uninode.smartcampus.modules.tickets.exception.TicketUnauthorizedException;
import com.uninode.smartcampus.modules.tickets.repository.TicketRepository;
import com.uninode.smartcampus.modules.users.entity.User;
import com.uninode.smartcampus.modules.users.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketAttachmentService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads/tickets}")
    private String uploadDir;

    private static final int MAX_ATTACHMENTS = 3;
    private static final String UPLOADS_PREFIX = "/uploads/tickets";

    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file, Long userId) throws IOException, InvalidFileTypeException {
        List<AttachmentResponse> res = uploadAttachments(ticketId, new MultipartFile[]{file}, userId);
        return res.get(0);
    }

    public List<AttachmentResponse> uploadAttachments(Long ticketId, MultipartFile[] files, Long userId) throws IOException, InvalidFileTypeException {
        if (files == null || files.length == 0) {
            throw new InvalidFileTypeException("No files provided");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        // Only ticket creator or ADMIN can upload
        Long ownerId = ticket.getRaisedUser() != null ? ticket.getRaisedUser().getUserId() : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new TicketUnauthorizedException("Only the ticket creator can upload attachments");
        }

        // Get current images list from JSONB
        List<String> currentImages = getImageList(ticket.getImages());
        if (currentImages.size() + files.length > MAX_ATTACHMENTS) {
            throw new MaxAttachmentsExceededException("Cannot upload more than " + MAX_ATTACHMENTS + " attachments per ticket");
        }

        List<AttachmentResponse> responses = new ArrayList<>();
        List<String> newPaths = new ArrayList<>(currentImages);

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);

            for (MultipartFile file : files) {
                validateFile(file);

                // Generate unique filename
                String extension = getFileExtension(file.getOriginalFilename());
                String uniqueName = UUID.randomUUID() + "." + extension;
                Path filePath = uploadPath.resolve(uniqueName);

                // Save file to disk
                Files.write(filePath, file.getBytes());

                // Store web path in JSONB
                String webPath = UPLOADS_PREFIX + "/" + uniqueName;
                newPaths.add(webPath);

                AttachmentResponse response = AttachmentResponse.builder()
                        .fileName(file.getOriginalFilename())
                        .filePath(webPath)
                        .fileSize(file.getSize())
                        .fileType(file.getContentType())
                        .build();
                responses.add(response);
            }

            // Update ticket with new images list
            ticket.setImages(objectMapper.writeValueAsString(newPaths));
            ticketRepository.save(ticket);

        } catch (IOException e) {
            // Cleanup: delete saved files if something goes wrong
            for (String path : newPaths) {
                if (!currentImages.contains(path)) {
                    try {
                        Files.deleteIfExists(Paths.get(uploadDir).resolve(getFileNameFromPath(path)));
                    } catch (IOException ignored) {}
                }
            }
            throw e;
        }

        return responses;
    }

    public List<AttachmentResponse> getAttachmentsByTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        List<String> imagePaths = getImageList(ticket.getImages());
        List<AttachmentResponse> responses = new ArrayList<>();

        for (int i = 0; i < imagePaths.size(); i++) {
            String imagePath = imagePaths.get(i);
            responses.add(AttachmentResponse.builder()
                    .fileName(getFileNameFromPath(imagePath))
                    .filePath(imagePath)
                    .index(i)
                    .build());
        }

        return responses;
    }

    // deleteAttachment removed: deletion of uploaded files is disabled per configuration.

    /**
     * Helper: Parse JSONB images column to List<String> of file paths
     */
    private List<String> getImageList(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty() || "null".equals(imagesJson)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Helper: Validate file type and size
     */
    private void validateFile(MultipartFile file) throws InvalidFileTypeException {
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("File is empty");
        }
        String contentType = file.getContentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType) && !"image/png".equalsIgnoreCase(contentType)) {
            throw new InvalidFileTypeException("Only PNG and JPEG images are allowed. Got: " + contentType);
        }
    }

    /**
     * Helper: Extract file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "bin";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Helper: Extract filename from web path
     */
    private String getFileNameFromPath(String webPath) {
        if (webPath == null || webPath.isEmpty()) {
            return "";
        }
        return webPath.substring(webPath.lastIndexOf("/") + 1);
    }
}
