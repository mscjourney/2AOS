package org.coms4156.tars.dto;

/**
 * Data Transfer Object for Client API responses.
 * <p>
 * Exposes only safe, external-facing client information to API consumers while
 * omitting internal or sensitive fields (e.g., API keys, secrets). This DTO is
 * used by controllers to serialize responses and by tests to validate payload
 * structure.
 * </p>
 */
public class ClientDto {
  /** Public identifier for the client resource. */
  private Long clientId;
  /** Display name for the client (non-unique, human-readable). */
  private String name;
  /** Registered contact email for the client. */
  private String email;
  /** Configured per-minute request limit for the client. */
  private int rateLimitPerMinute;
  /** Maximum concurrent requests allowed for the client. */
  private int maxConcurrentRequests;

  /**
   * {@code ClientDto} Constructs an empty ClientDto instance for deserialization.
   * <p>
   * Required by JSON serializers (e.g., Jackson) to instantiate the DTO during
   * deserialization.
   * </p>
   */
  public ClientDto() {
    // Default constructor for JSON deserialization
  }

  /**
   * {@code ClientDto} Full-args constructor for manual instantiation.
   *
   * @param clientId public client identifier
   * @param name client display name
   * @param email registered client email
   * @param rateLimitPerMinute configured per-minute request limit
   * @param maxConcurrentRequests allowed maximum concurrent requests
   */
  public ClientDto(Long clientId, String name, String email,
                   int rateLimitPerMinute, int maxConcurrentRequests) {
    this.clientId = clientId;
    this.name = name;
    this.email = email;
    this.rateLimitPerMinute = rateLimitPerMinute;
    this.maxConcurrentRequests = maxConcurrentRequests;
  }

  /**
   * {@code getClientId} Returns the public client identifier used in API payloads.
   */
  public Long getClientId() {
    return clientId;
  }

  /**
   * {@code setClientId} Sets the public client identifier.
   *
   * @param clientId public client identifier
   */
  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  /**
   * {@code getName} Returns the client display name.
   */
  public String getName() {
    return name;
  }

  /**
   * {@code setName} Sets the client display name.
   *
   * @param name human-readable client name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@code getEmail} Returns the registered contact email for the client.
   */
  public String getEmail() {
    return email;
  }

  /**
   * {@code setEmail} Sets the registered client email.
   *
   * @param email contact email for the client
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * {@code getRateLimitPerMinute} Returns configured per-minute request limit.
   */
  public int getRateLimitPerMinute() {
    return rateLimitPerMinute;
  }

  /**
   * {@code setRateLimitPerMinute} Sets the per-minute request limit.
   *
   * @param rateLimitPerMinute maximum number of requests per minute
   */
  public void setRateLimitPerMinute(int rateLimitPerMinute) {
    this.rateLimitPerMinute = rateLimitPerMinute;
  }

  /**
   * {@code getMaxConcurrentRequests} Returns allowed max concurrent requests.
   */
  public int getMaxConcurrentRequests() {
    return maxConcurrentRequests;
  }
  
  /**
   * {@code setMaxConcurrentRequests} Sets the allowed concurrent requests.
   *
   * @param maxConcurrentRequests maximum concurrent requests permitted
   */
  public void setMaxConcurrentRequests(int maxConcurrentRequests) {
    this.maxConcurrentRequests = maxConcurrentRequests;
  }
}
