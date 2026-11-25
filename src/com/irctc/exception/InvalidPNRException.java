package com.irctc.exception;

/**
 * Thrown when an invalid or nonexistent PNR is searched or cancelled.
 */
public class InvalidPNRException extends TatkalException {
    public InvalidPNRException(String message) {
        super(message);
    }
}
