package com.spantry.inventory.service.exception;

/**
 * Runtime exception thrown when an operation attempts to access an inventory item that does not
 * exist (e.g., finding or deleting by a non-existent ID).
 */
public class ItemNotFoundException extends RuntimeException {

  // Recommended for Serializable classes
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new ItemNotFoundException with the specified detail message.
   *
   * @param message the detail message.
   */
  public ItemNotFoundException(final String message) {
    super(message);
  }

  /**
   * Constructs a new ItemNotFoundException with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   *     (A {@code null} value is permitted, and indicates that the cause is nonexistent or
   *     unknown.)
   */
  public ItemNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
