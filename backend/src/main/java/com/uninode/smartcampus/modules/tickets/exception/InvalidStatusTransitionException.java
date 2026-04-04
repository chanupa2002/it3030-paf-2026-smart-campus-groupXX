package com.uninode.smartcampus.modules.tickets.exception;

import com.uninode.smartcampus.modules.tickets.entity.TicketStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
