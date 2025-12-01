package org.coms4156.tars.dto;

/**
 * Data Transfer Object for TarsUser responses.
 */
public class TarsUserDto {
  private Long userId;
  private Long clientId;
  private String username;
  private String email;
  private String role;
  private boolean active;
  private String signUpDate;
  private String lastLogin;

  public TarsUserDto() { }

  public TarsUserDto(Long userId, Long clientId, String username, String email,
                     String role, boolean active, String signUpDate, String lastLogin) {
    this.userId = userId;
    this.clientId = clientId;
    this.username = username;
    this.email = email;
    this.role = role;
    this.active = active;
    this.signUpDate = signUpDate;
    this.lastLogin = lastLogin;
  }

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getClientId() { return clientId; }
  public void setClientId(Long clientId) { this.clientId = clientId; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getRole() { return role; }
  public void setRole(String role) { this.role = role; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public String getSignUpDate() { return signUpDate; }
  public void setSignUpDate(String signUpDate) { this.signUpDate = signUpDate; }
  public String getLastLogin() { return lastLogin; }
  public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
}
