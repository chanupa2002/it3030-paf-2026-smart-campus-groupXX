package com.uninode.smartcampus.modules.tickets.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.uninode.smartcampus.modules.tickets.dto.AddCommentRequest;
import com.uninode.smartcampus.modules.tickets.dto.CommentResponse;
import com.uninode.smartcampus.modules.tickets.service.TicketCommentService;
import com.uninode.smartcampus.modules.users.entity.User;
import com.uninode.smartcampus.modules.users.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentManagementController {

    private final TicketCommentService commentService;
    private final UserRepository userRepository;

    /**
     * Edit a comment (comment owner only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponse> editComment(
            @PathVariable Long id,
            @Valid @RequestBody AddCommentRequest request,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        CommentResponse response = commentService.editComment(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a comment (comment owner only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = resolveUserId(authentication);
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }

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
        // Fallback: try to resolve by authentication name
        String name = authentication.getName();
        return userRepository.findByEmail(name)
                .or(() -> userRepository.findByUsername(name))
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found by name"))
                .getUserId();
    }
}
