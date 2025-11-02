package org.coms4156.tars.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code TarsUser} Represents a user in the Tars system.
 */
public class TarsUser {

    private Long userId;
    private Long clientId;
    private String username;
    private String role;
    private boolean active;
    private String signUpDate;
    private String lastLogin;


    @JsonCreator
    public TarsUser(
            @JsonProperty("clientId") Long clientId,
            @JsonProperty("username") String username,
            @JsonProperty("role") String role
            ) {
        this.clientId = clientId;
        this.username = username;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
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
        return "TarsUser{" +
                "userId=" + userId +
                ", clientId=" + clientId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", signUpDate='" + signUpDate + '\'' +
                ", lastLogin='" + lastLogin + '\'' +
                '}';
    }

    // how to send a json object {"clientId": 1233, "username": "bob", "role": user}in post request java spring boot endpoint
}
