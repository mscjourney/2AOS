package org.coms4156.tars.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code TarsUser} Represents a user in the Tars system.
 */
public class TarsUser {

  @JsonProperty("userId")
  private Long userId;
  
  @JsonProperty("clientId")
  private Long clientId;
  
  @JsonProperty("username")
  private String username;

  @JsonProperty("email")
  private String email;
  
  @JsonProperty("role")
  private String role;
  
  @JsonProperty("active")
  private boolean active;
  
  @JsonProperty("signUpDate")
  private String signUpDate;
  
  @JsonProperty("lastLogin")
  private String lastLogin;


  /**
   * {@code TarsUser} empty constructor. Initializes a new instance of TarsUser.
   */
  public TarsUser() {
    this.userId = null;
    this.clientId = null;
    this.username = "";
    this.email = "";
    this.role = "user"; // Default role
    this.active = true; // Default to active
    this.signUpDate = "";
    this.lastLogin = "";
  }

  /**
   * {@code TarsUser} parameterized constructor.
   * Initializes a new instance of TarsUser with specified values.
   *
   * @param clientId The client ID associated with the user.
   * @param username The username of the user.
   * @param role The role of the user (e.g., "admin", "user").
   */
  @JsonCreator
  public TarsUser(
        @JsonProperty("clientId") Long clientId,
        @JsonProperty("username") String username,
        @JsonProperty("email") String email,
        @JsonProperty("role") String role) {
    this.clientId = clientId;
    this.username = username;
    this.email = email;
    this.role = role;
    this.active = true; // Default to active when created
    this.signUpDate = "";
    this.lastLogin = "";
    this.userId = null; // userId to be set later
  }

  // Getters and Setters
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getClientId() {
    return clientId;
  }

  public void setClientId(Long clientId) {
    this.clientId = clientId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getSignUpDate() {
    return signUpDate;
  }

  public void setSignUpDate(String signUpDate) {
    this.signUpDate = signUpDate;
  }

  public String getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(String lastLogin) {
    this.lastLogin = lastLogin;
  }

  @Override
  public String toString() {
    return "TarsUser{"
      + "userId= " + userId + ", clientId= " + clientId
      + ", username= '" + username + "', email= '" + email + "'"
      + ", role='" + role + "'"
      + ", active=" + active + ", signUpDate= '" + signUpDate + "'"
      + ", lastLogin= '" + lastLogin + "'" + '}';
  }
}
