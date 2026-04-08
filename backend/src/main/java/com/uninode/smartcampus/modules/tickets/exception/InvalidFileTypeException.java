package com.uninode.smartcampus.modules.tickets.exception;

public class InvalidFileTypeException extends RuntimeException {
    public InvalidFileTypeException(String message) {
        super(message);
    }

    public InvalidFileTypeException() {
        super("Invalid file type. Only JPG and PNG files are allowed.");
    }
}
