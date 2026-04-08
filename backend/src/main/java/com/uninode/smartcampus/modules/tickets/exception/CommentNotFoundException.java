package com.uninode.smartcampus.modules.tickets.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(String message) {
        super(message);
    }

    public CommentNotFoundException(Long commentId) {
        super("Comment with ID " + commentId + " not found");
    }
}
