package com.irctc.exception;

/**
 * Thrown when booking is requested outside the 10:00:00 AM Tatkal window.
 */
public class TatkalWindowClosedException extends TatkalException {
    public TatkalWindowClosedException(String message) {
        super(message);
    }
}
