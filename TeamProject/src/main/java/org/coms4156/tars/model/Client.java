package org.coms4156.tars.model;

/**
 * {@code Client} class represents a client with an IP address, port, and client ID.
 */
public class Client {
    private String ipAddress;
    private int port;
    private int clientId;
    
    /**
     * Creates a new {@code Client} with the specified parameters.
     *
     * @param clientId the unique identifier for the client
     * @param ipAddress the IP address of the client
     * @param port the port number of the client
     */
    public Client(int clientId, String ipAddress, int port) {
        this.clientId = clientId;
        this.ipAddress = ipAddress;
        this.port = port;
    }
    
    /**
     * No-args constructor.
     */
    public Client() {}
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public int getPort() {
        return port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }

    public int getClientId() {
        return clientId;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }
    
}
