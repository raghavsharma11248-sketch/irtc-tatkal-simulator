package com.irctc.exception;

/**
 * Base custom checked exception for all Tatkal booking errors.
 * Satisfies Syllabus Lecture 3 (Checked/Unchecked Exceptions & Custom Exceptions).
 */
public class TatkalException extends Exception {
    public TatkalException(String message) {
        super(message);
    }

    public TatkalException(String message, Throwable cause) {
        super(message, cause);
    }
}
