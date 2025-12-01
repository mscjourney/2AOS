package org.coms4156.tars.exception;

/** Resource not found (404) domain exception. */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) { super(message); }
}
