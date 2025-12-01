package org.coms4156.tars.exception;

/**
 * Domain exception representing a client error resulting in HTTP 400.
 */
public class BadRequestException extends RuntimeException {

  /**
   * Constructs a new BadRequestException with the provided detail message.
   *
   * @param message human readable explanation of the failure
   */
  public BadRequestException(String message) {
    super(message);
  }
}
