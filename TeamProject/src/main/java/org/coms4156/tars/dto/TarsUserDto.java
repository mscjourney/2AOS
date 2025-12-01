package org.coms4156.tars.dto;

/**
 * Data Transfer Object for TarsUser responses.
 * <p>
 * Provides a safe, external-facing representation of a user without exposing
 * internal fields. Used by controllers for response serialization and tests
 * for validating payload shape.
 * </p>
 */
public class TarsUserDto {
  /** Public identifier of the user. */
  private Long userId;
  /** Public identifier of the owning client. */
  private Long clientId;
  /** Username (unique within a client). */
  private String username;
  /** Registered contact email for the user. */
  private String email;
  /** Role assigned to the user (e.g., admin, user). */
  private String role;
  /** Current activation status of the user. */
  private boolean active;
  /** ISO-8601 sign-up date string. */
  private String signUpDate;
  /** ISO-8601 last login timestamp string. */
  private String lastLogin;

  /**
   * {@code TarsUserDto} Default constructor for JSON deserialization.
   * <p>
   * Required by JSON serializers (e.g., Jackson) to instantiate during
   * deserialization.
   * </p>
   */
  public TarsUserDto() {
  }

  /**
   * {@code TarsUserDto} Full-args constructor.
   *
   * @param userId public user identifier
   * @param clientId owning client identifier
   * @param username unique username within client
   * @param email registered user email
   * @param role assigned role
   * @param active activation status
   * @param signUpDate ISO-8601 sign-up date string
   * @param lastLogin ISO-8601 last login timestamp string
   */
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

  /**
   * {@code getUserId} Returns the public user identifier.
   */
  public Long getUserId() {
    return userId;
  }

  /**
   * {@code setUserId} Sets the user identifier.
   *
   * @param userId public user identifier
   */
  public void setUserId(Long userId) {
    this.userId = userId;
  }

  /**
   * {@code getClientId} Returns the owning client identifier.
   */
  public Long getClientId() {
    return clientId;
  }

  /**
   * {@code setClientId} Sets the owning client identifier.
   *
   * @param clientId public client identifier
   */
  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  /**
   * {@code getUsername} Returns the user's username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * {@code setUsername} Sets the user's username.
   *
   * @param username unique username within client
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * {@code getEmail} Returns the user's email.
   */
  public String getEmail() {
    return email;
  }

  /**
   * {@code setEmail} Sets the user's email.
   *
   * @param email registered user email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * {@code getRole} Returns the assigned role.
   */
  public String getRole() {
    return role;
  }

  /**
   * {@code setRole} Sets the assigned role.
   *
   * @param role role name
   */
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * {@code isActive} Returns whether the user is active.
   */
  public boolean isActive() {
    return active;
  }

  /**
   * {@code setActive} Sets the activation status.
   *
   * @param active true if active
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  /**
   * {@code getSignUpDate} Returns the sign-up date.
   */
  public String getSignUpDate() {
    return signUpDate;
  }

  /**
   * {@code setSignUpDate} Sets the sign-up date.
   *
   * @param signUpDate ISO-8601 date string
   */
  public void setSignUpDate(String signUpDate) {
    this.signUpDate = signUpDate;
  }

  /**
   * {@code getLastLogin} Returns the last login timestamp.
   */
  public String getLastLogin() {
    return lastLogin;
  }

  /**
   * {@code setLastLogin} Sets the last login timestamp.
   *
   * @param lastLogin ISO-8601 timestamp string
   */
  public void setLastLogin(String lastLogin) {
    this.lastLogin = lastLogin;
  }
}
