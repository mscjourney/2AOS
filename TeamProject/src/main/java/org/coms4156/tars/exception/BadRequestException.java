package org.coms4156.tars.exception;

/** Bad request (400) domain exception. */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) { super(message); }
}
