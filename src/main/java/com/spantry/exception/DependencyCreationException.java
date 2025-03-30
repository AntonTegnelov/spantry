package com.spantry.exception;

/** Exception indicating a failure during dependency creation, typically via reflection. */
public class DependencyCreationException extends RuntimeException {

  private static final long serialVersionUID = 1L; // Recommended for RuntimeExceptions

  /**
   * Constructs a new dependency creation exception with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause.
   */
  public DependencyCreationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
