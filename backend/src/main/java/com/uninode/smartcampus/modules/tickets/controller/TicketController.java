package com.uninode.smartcampus.modules.tickets.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.uninode.smartcampus.modules.tickets.dto.AssignTechnicianRequest;
import com.uninode.smartcampus.modules.tickets.dto.AddResolutionNotesRequest;
import com.uninode.smartcampus.modules.tickets.dto.CreateTicketRequest;
import com.uninode.smartcampus.modules.tickets.dto.TicketFilterRequest;
import com.uninode.smartcampus.modules.tickets.dto.TicketResponse;
import com.uninode.smartcampus.modules.tickets.dto.UpdateTicketStatusRequest;
import com.uninode.smartcampus.modules.tickets.service.TicketService;
import com.uninode.smartcampus.modules.tickets.service.TicketAttachmentService;
import com.uninode.smartcampus.modules.tickets.exception.InvalidFileTypeException;
import com.uninode.smartcampus.modules.users.entity.User;
import com.uninode.smartcampus.modules.users.repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final TicketAttachmentService ticketAttachmentService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create a new ticket (any authenticated user)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated() and !hasRole('TECHNICIAN')")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        TicketResponse response = ticketService.createTicket(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated() and !hasRole('TECHNICIAN')")
    public ResponseEntity<TicketResponse> createTicketMultipart(
            @RequestPart("ticket") String ticketJson,
            @RequestPart(value = "file", required = false) MultipartFile[] files,
            Authentication authentication) throws IOException, InvalidFileTypeException {
        Long userId = resolveUserId(authentication);
        CreateTicketRequest request = objectMapper.readValue(ticketJson, CreateTicketRequest.class);
        TicketResponse response = ticketService.createTicket(request, userId);

        if (files != null && files.length > 0) {
            ticketAttachmentService.uploadAttachments(response.getTicketId(), files, userId);
            // Fetch updated ticket with images
            boolean isAdmin = false;
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                isAdmin = user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            }
            response = ticketService.getTicket(response.getTicketId(), userId, isAdmin);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get ticket details by ID (authorized users only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        TicketResponse response = ticketService.getTicket(id, user.getUserId(), isAdmin);
        log.info("Retrieved ticket {}: {} attachments found", id, response.getAttachments() != null ? response.getAttachments().size() : 0);
        if (response.getAttachments() != null && !response.getAttachments().isEmpty()) {
            response.getAttachments().forEach(att -> log.info("  - Attachment: {} - Path: {}", att.getFileName(), att.getFilePath()));
        }
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        try {
            if (principal instanceof User) {
                return ((User) principal).getUserId();
            }
        } catch (ClassCastException ignored) {
        }
        String name = authentication.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found by name"))
                .getUserId();
    }

    /**
     * Get all tickets with filtering (role-based)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<TicketResponse>> getAllTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            Authentication authentication) {
        Long currentUserId = resolveUserId(authentication);
        // Prefer checking the persisted user's roleName where possible to avoid mismatches
        String roleName = null;
        try {
            roleName = userRepository.findById(currentUserId)
                .map(u -> u.getUserType() != null ? u.getUserType().getRoleName() : null)
                .orElse(null);
        } catch (Exception ex) {
            // fallback to authorities if repository lookup fails
            roleName = null;
        }

        boolean isAdmin = false;
        boolean isTechnician = false;
        if (roleName != null) {
            isAdmin = "admin".equalsIgnoreCase(roleName.trim());
            isTechnician = "technician".equalsIgnoreCase(roleName.trim());
        } else {
            isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
            isTechnician = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_TECHNICIAN") || a.getAuthority().equalsIgnoreCase("TECHNICIAN"));
        }

        TicketFilterRequest filter = new TicketFilterRequest();
        if (status != null) {
            com.uninode.smartcampus.modules.tickets.entity.TicketStatus parsed = com.uninode.smartcampus.modules.tickets.entity.TicketStatus.fromString(status);
            filter.setStatus(parsed);
        }
        if (priority != null) {
            com.uninode.smartcampus.modules.tickets.entity.Priority parsedPriority = com.uninode.smartcampus.modules.tickets.entity.Priority.fromString(priority);
            filter.setPriority(parsedPriority);
        }
        filter.setPageNumber(pageNumber);
        filter.setPageSize(pageSize);
        filter.setSortBy(sortBy);
        filter.setSortOrder(sortOrder);

        log.debug("getAllTickets called by userId={} isAdmin={} isTechnician={} authorities={} statusParam={} priorityParam={} parsedStatus={} parsedPriority={}",
            currentUserId,
            isAdmin,
            isTechnician,
            authentication.getAuthorities(),
            status,
            priority,
            filter.getStatus(),
            filter.getPriority());

        Page<TicketResponse> response = ticketService.getAllTickets(filter, currentUserId, isAdmin, isTechnician);
        return ResponseEntity.ok(response);
    }

    /**
     * Update ticket status (admin or assigned technician)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        TicketResponse response = ticketService.updateTicketStatus(id, request, user.getUserId(), isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * Assign technician to ticket (admin only)
     */
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketResponse> assignTechnician(
            @PathVariable Long id,
            @Valid @RequestBody AssignTechnicianRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TicketResponse response = ticketService.assignTechnician(id, request, user.getUserId());
        return ResponseEntity.ok(response);
    }

        /**
         * Debug endpoint: returns resolved user id and role flags for the current auth.
         * Use this to verify which user the server thinks is authenticated.
         */
        @GetMapping("/whoami")
        @PreAuthorize("isAuthenticated()")
        public ResponseEntity<?> whoami(Authentication authentication) {
        Long currentUserId = resolveUserId(authentication);
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
        boolean isTechnician = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_TECHNICIAN") || a.getAuthority().equalsIgnoreCase("TECHNICIAN"));

        String persistedRole = null;
        try {
            persistedRole = userRepository.findById(currentUserId)
                .map(u -> u.getUserType() != null ? u.getUserType().getRoleName() : null)
                .orElse(null);
        } catch (Exception ignored) {}

        return ResponseEntity.ok(Map.of(
            "currentUserId", currentUserId,
            "isAdmin", isAdmin,
            "isTechnician", isTechnician,
            "persistedRole", persistedRole,
            "authorities", authentication.getAuthorities()
        ));
        }

    /**
     * Add resolution notes (assigned technician only)
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TicketResponse> addResolutionNotes(
            @PathVariable Long id,
            @Valid @RequestBody AddResolutionNotesRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TicketResponse response = ticketService.addResolutionNotes(id, request, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete ticket (admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ticketService.deleteTicket(id, user.getUserId(), true);
        return ResponseEntity.noContent().build();
    }

    /**
     * DEBUG ENDPOINT: Get current user info and authorities
     * This helps diagnose authorization issues
     */

}
