package com.irctc.model;

/**
 * Defines current lifecycle status of a Tatkal ticket.
 */
public enum BookingStatus {
    CONFIRMED("CNF", "Confirmed Berth Allocated"),
    RAC("RAC", "Reservation Against Cancellation"),
    WAITING_LIST("WL", "Waiting List (Priority Queued)"),
    CANCELLED("CAN", "Cancelled & Refund Processed");

    private final String code;
    private final String description;

    BookingStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
