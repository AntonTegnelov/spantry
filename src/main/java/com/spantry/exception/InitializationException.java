package com.spantry.exception;

/** Custom unchecked exception for application initialization errors. */
public class InitializationException extends RuntimeException {

  /** Default serial version ID. */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new InitializationException with the specified detail message and cause.
   *
   * @param message the detail message.
   * @param cause the cause.
   */
  public InitializationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new InitializationException with the specified detail message.
   *
   * @param message the detail message.
   */
  public InitializationException(final String message) {
    super(message);
  }
}
