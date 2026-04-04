package com.uninode.smartcampus.modules.tickets.exception;

public class TicketNotFoundException extends RuntimeException {
    public TicketNotFoundException(String message) {
        super(message);
    }

    public TicketNotFoundException(Long ticketId) {
        super("Ticket with ID " + ticketId + " not found");
    }
}
