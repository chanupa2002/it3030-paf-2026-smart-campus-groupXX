package com.uninode.smartcampus.modules.tickets.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.uninode.smartcampus.modules.tickets.dto.AddCommentRequest;
import com.uninode.smartcampus.modules.tickets.dto.CommentResponse;
import com.uninode.smartcampus.modules.tickets.service.TicketCommentService;
import com.uninode.smartcampus.modules.users.entity.User;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final TicketCommentService commentService;

    /**
     * Add a comment to a ticket (authenticated users)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CommentResponse response = commentService.addComment(ticketId, request, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all comments for a ticket
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long ticketId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        Page<CommentResponse> response = commentService.getCommentsByTicket(ticketId, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }
}
