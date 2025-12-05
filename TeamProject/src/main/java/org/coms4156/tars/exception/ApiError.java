package org.coms4156.tars.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API error payload returned by the global exception handler.
 * Provides basic metadata plus optional validation/detail messages.
 */
public class ApiError {
  private Instant timestamp = Instant.now();
  private int status;
  private String error;
  private String message;
  private String path;
  private List<String> details;

  /**
   * Empty constructor required for JSON (de)serialization frameworks.
   */
  public ApiError() {
    // intentionally empty
  }

  /**
   * Full constructor for building an error response.
   *
   * @param status  numeric HTTP status code (e.g., 404)
   * @param error   short status description (e.g., "Not Found")
   * @param message primary human readable error message
   * @param path    request path related to the error
   * @param details optional list of fine‑grained error details
   */
  public ApiError(int status, String error, String message, String path, List<String> details) {
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.details = details;
  }

  /**
   * Timestamp when the error payload was created.
   *
   * @return creation instant
   */
  public Instant getTimestamp() {
    return timestamp;
  }

  /**
   * HTTP status code.
   *
   * @return status code
   */
  public int getStatus() {
    return status;
  }

  /**
   * Sets the HTTP status code.
   *
   * @param status status code
   */
  public void setStatus(int status) {
    this.status = status;
  }

  /**
   * Short HTTP reason phrase.
   *
   * @return error label
   */
  public String getError() {
    return error;
  }

  /**
   * Sets the short HTTP reason phrase.
   *
   * @param error phrase
   */
  public void setError(String error) {
    this.error = error;
  }

  /**
   * Human readable message.
   *
   * @return message text
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the human readable message.
   *
   * @param message detail message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Request path that produced the error.
   *
   * @return path string
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the request path.
   *
   * @param path request path
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Optional list of granular details (e.g., validation errors).
   *
   * @return list of details or null
   */
  public List<String> getDetails() {
    return details;
  }

  /**
   * Sets the list of granular details.
   *
   * @param details list of details
   */
  public void setDetails(List<String> details) {
    this.details = details;
  }
}
