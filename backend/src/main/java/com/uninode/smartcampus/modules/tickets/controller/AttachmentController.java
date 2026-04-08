package com.uninode.smartcampus.modules.tickets.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import com.uninode.smartcampus.modules.tickets.dto.AttachmentResponse;
import com.uninode.smartcampus.modules.tickets.service.TicketAttachmentService;
import com.uninode.smartcampus.modules.users.entity.User;
import com.uninode.smartcampus.modules.users.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class AttachmentController {

    private final TicketAttachmentService attachmentService;
    private final UserRepository userRepository;

    /**
     * Upload one or more attachments to a ticket (ticket creator only).
     * Accepts multiple files under the same form name `file`.
     */
    @PostMapping("/{ticketId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<java.util.List<AttachmentResponse>> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile[] files,
            Authentication authentication) throws IOException {
        Long userId = resolveUserId(authentication);
        java.util.List<AttachmentResponse> responses = attachmentService.uploadAttachments(ticketId, files, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Get all attachments for a ticket
     */
    @GetMapping("/{ticketId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Long ticketId) {
        List<AttachmentResponse> response = attachmentService.getAttachmentsByTicket(ticketId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an attachment from a ticket by index
     */
    // DELETE endpoint removed per request; attachments can still be managed via upload and ticket updates.

    private Long resolveUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        try {
            if (principal instanceof User) {
                return ((User) principal).getUserId();
            }
            if (principal instanceof UserDetails) {
                String name = authentication.getName();
                return userRepository.findByUsername(name)
                        .or(() -> userRepository.findByEmail(name))
                        .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
                        .getUserId();
            }
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                String email = jwt.getClaimAsString("email");
                if (email != null) {
                    return userRepository.findByEmail(email)
                            .orElseThrow(() -> new IllegalArgumentException("Authenticated JWT user not found"))
                            .getUserId();
                }
                String sub = jwt.getSubject();
                if (sub != null) {
                    return userRepository.findByUsername(sub)
                            .or(() -> userRepository.findByEmail(sub))
                            .orElseThrow(() -> new IllegalArgumentException("Authenticated JWT subject not found"))
                            .getUserId();
                }
            }
        } catch (ClassCastException ignored) {
        }
        String name = authentication.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found by name"))
                .getUserId();
    }
}
