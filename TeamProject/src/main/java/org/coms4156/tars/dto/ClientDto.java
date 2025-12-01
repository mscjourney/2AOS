package org.coms4156.tars.dto;

/**
 * Data Transfer Object for Client API responses.
 * Omits internal/secret fields (e.g., apiKey) by design.
 */
public class ClientDto {
  private Long clientId;
  private String name;
  private String email;
  private int rateLimitPerMinute;
  private int maxConcurrentRequests;

  public ClientDto() { }

  public ClientDto(Long clientId, String name, String email,
                   int rateLimitPerMinute, int maxConcurrentRequests) {
    this.clientId = clientId;
    this.name = name;
    this.email = email;
    this.rateLimitPerMinute = rateLimitPerMinute;
    this.maxConcurrentRequests = maxConcurrentRequests;
  }

  public Long getClientId() { return clientId; }
  public void setClientId(Long clientId) { this.clientId = clientId; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public int getRateLimitPerMinute() { return rateLimitPerMinute; }
  public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
  public int getMaxConcurrentRequests() { return maxConcurrentRequests; }
  public void setMaxConcurrentRequests(int maxConcurrentRequests) { this.maxConcurrentRequests = maxConcurrentRequests; }
}
