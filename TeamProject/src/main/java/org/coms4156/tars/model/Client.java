package org.coms4156.tars.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@code Client} A class to represent a client model in the Tars system.
 */
public class Client {

    @JsonProperty("clientId")
    private Long clientId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("rateLimitPerMinute")
    private int rateLimitPerMinute;
    
    @JsonProperty("maxConcurrentRequests")
    private int maxConcurrentRequests;

    /**
     * {@code Client} empty constructor. Initializes a new instance of Client.
     */
    public Client() {
        this.clientId = null;
        this.name = "";
        this.apiKey = "";
        this.rateLimitPerMinute = 60; // Default value
        this.maxConcurrentRequests = 5; // Default value
    }

    /**
     * {@code Client} parameterized constructor. Initializes a new instance of Client with specified values.
     *
     * @param clientId The unique identifier for the client.
     * @param name The name of the client.
     * @param apiKey The API key associated with the client.
     */
    public Client(Long clientId, String name, String apiKey) {
        this.clientId = clientId;
        this.name = name;
        this.apiKey = apiKey;
        this.rateLimitPerMinute = 60; // Default value
        this.maxConcurrentRequests = 5; // Default value
    }

    // Getters and Setters
    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    public void setMaxConcurrentRequests(int maxConcurrentRequests) {
        this.maxConcurrentRequests = maxConcurrentRequests;
    }

    @Override
    public String toString() {
        return "Client{" +
                "clientId=" + clientId +
                ", name='" + name + '\'' +
                ", rateLimitPerMinute=" + rateLimitPerMinute +
                ", maxConcurrentRequests=" + maxConcurrentRequests +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Client client = (Client) obj;
        return clientId != null && clientId.equals(client.clientId);
    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }
}

