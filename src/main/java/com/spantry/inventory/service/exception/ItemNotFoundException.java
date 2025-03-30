package com.spantry.inventory.service.exception;

/**
 * Custom runtime exception thrown when an operation attempts to access
 * an inventory item that does not exist.
 */
public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(String message) {
        super(message);
    }

    public ItemNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 