package org.coms4156.tars.exception;

/**
 * {@code ConflictException} indicates a request could not be completed due to a resource
 * state conflict (e.g., duplicate name or email). Mapped to HTTP 409.
 */
public class ConflictException extends RuntimeException {
  /**
   * Constructs a new {@code ConflictException} with the specified detail message.
   *
   * @param message human readable conflict explanation
   */
  public ConflictException(String message) {
    super(message);
  }
}
