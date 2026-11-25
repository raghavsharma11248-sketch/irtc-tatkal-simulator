package com.irctc.exception;

/**
 * Thrown when Confirmed, RAC, and Waiting List quotas are completely exhausted.
 */
public class SeatNotAvailableException extends TatkalException {
    public SeatNotAvailableException(String message) {
        super(message);
    }
}
