package org.coms4156.tars.exception;

/**
 * {@code ForbiddenException} indicates the server understood the request but refuses to
 * authorize it (e.g., inactive user attempting login). Mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
  /**
   * Constructs a new {@code ForbiddenException} with the specified detail message.
   *
   * @param message human readable explanation of the forbidden condition
   */
  public ForbiddenException(String message) {
    super(message);
  }
}
