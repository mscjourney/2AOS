package org.coms4156.tars.exception;

/**
 * Domain exception indicating a requested resource was not found (HTTP 404).
 */
public class NotFoundException extends RuntimeException {

  /**
   * Constructs a new NotFoundException with the provided detail message.
   *
   * @param message human readable explanation of which resource is missing
   */
  public NotFoundException(String message) {
    super(message);
  }
}
