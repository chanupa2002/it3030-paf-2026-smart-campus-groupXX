package com.uninode.smartcampus.modules.tickets.exception;

public class MaxAttachmentsExceededException extends RuntimeException {
    public MaxAttachmentsExceededException(String message) {
        super(message);
    }

    public MaxAttachmentsExceededException() {
        super("Maximum number of attachments (3) exceeded");
    }
}
