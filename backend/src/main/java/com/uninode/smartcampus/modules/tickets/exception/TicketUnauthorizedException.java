package com.uninode.smartcampus.modules.tickets.exception;

public class TicketUnauthorizedException extends RuntimeException {
    public TicketUnauthorizedException(String message) {
        super(message);
    }
}
