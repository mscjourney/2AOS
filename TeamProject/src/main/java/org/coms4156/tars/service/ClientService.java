package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@code ClientService} class for managing {@code Client} entities.
 * Handles persistence of client data to/from a JSON file.
 */
@Service
public class ClientService {

  private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
  private final File clientFile;
  private final ObjectMapper mapper = new ObjectMapper();
  private List<Client> clients;

  /**
   * Constructor with path injected from application properties.
   * If the property 'tars.client.data.path' is not set,
   * defaults to ./data/ClientData.json (writable location).
   *
   * @param clientFilePath the path to the client data JSON file
   */
  public ClientService(
      @Value("${tars.client.data.path:./data/clients.json}") String clientFilePath) {
    this.clientFile = new File(clientFilePath);
    if (!clientFile.exists()) {
      try {
        File parent = clientFile.getParentFile();
        if (parent != null) {
          parent.mkdirs();
        }
        mapper.writeValue(clientFile, new ArrayList<Client>());
        if (logger.isInfoEnabled()) {
          logger.info("Created new client data file at: {}", clientFile.getAbsolutePath());
        }
      } catch (IOException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to create client data file: {}", clientFilePath, e);
        }
      }
    } else {
      if (logger.isInfoEnabled()) {
        logger.info("Using existing client data file at: {}", clientFile.getAbsolutePath());
      }
    }
    this.clients = loadData();
  }

  /**
   * Loads the existing client data from the JSON file.
   *
   * @return a list of {@code Client} objects
   */
  private List<Client> loadData() {
    try {
      return mapper.readValue(this.clientFile, new TypeReference<List<Client>>() {});
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to load clients from {}", clientFile.getPath(), e);
      }
      return new ArrayList<>();
    }
  }

  /**
   * Writes the current list of clients to the JSON file.
   */
  public synchronized void saveData() {
    try {
      mapper.writeValue(this.clientFile, clients);
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to write clients to {}", clientFile.getPath(), e);
      }
    }
  }

  /**
   * Returns a defensive copy of the list of clients stored in the service.
   *
   * @return a List of {@code Client} objects representing all clients
   */
  public synchronized List<Client> getClientList() {
    if (clients == null) {
      clients = loadData();
    }
    return new ArrayList<>(clients);  // Return defensive copy
  }

  /**
   * Retrieves a {@code Client} by its clientId.
   *
   * @param clientId the unique identifier for the client
   * @return the {@code Client} object if found, or null if not found
   */
  public synchronized Client getClient(int clientId) {
    if (clients == null) {
      clients = loadData();
    }

    if (clientId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("Client Id cannot be negative");
      }
      return null;
    }

    for (Client client : clients) {
      if (client.getClientId() == clientId) {
        return client;
      }
    }

    return null;
  }

  /**
   * Generates the next available client ID based on the highest existing ID.
   *
   * @return the next available client ID (starts at 1 if no clients exist)
   */
  private synchronized int getNextClientId() {
    if (clients == null) {
      clients = loadData();
    }
    
    int maxId = 0;
    for (Client client : clients) {
      if (client.getClientId() > maxId) {
        maxId = client.getClientId();
      }
    }
    
    int nextId = maxId + 1;
    if (logger.isDebugEnabled()) {
      logger.debug("Generated next client ID: {}", nextId);
    }
    return nextId;
  }

  /**
   * Creates a new client with an auto-generated ID.
   * The client ID is automatically assigned based on the last used ID in the datastore.
   *
   * @param ipAddress the IP address of the new client
   * @param port the port number of the new client
   * @return the newly created {@code Client} object with auto-generated ID
   */
  public synchronized Client createClient(String ipAddress, int port) {
    if (clients == null) {
      clients = loadData();
    }
    
    int newClientId = getNextClientId();
    Client newClient = new Client(newClientId, ipAddress, port);
    
    clients.add(newClient);
    saveData();
    
    if (logger.isInfoEnabled()) {
      logger.info("Client created successfully with auto-generated id={} ip={} port={}", 
          newClientId, ipAddress, port);
    }
    
    return newClient;
  }

  /**
   * Adds a new client to the data store with a pre-specified client ID.
   * Use {@link #createClient(String, int)} for auto-generated IDs.
   *
   * @param newClient the {@code Client} object to be added
   * @return true if the client was successfully added, false if the clientId already exists
   *         or if the client is null
   */
  public synchronized boolean addClient(Client newClient) {
    if (clients == null) {
      clients = loadData();
    }
    if (newClient == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to add null client");
      }
      return false;
    }
    for (Client client : clients) {
      if (client.getClientId() == newClient.getClientId()) {
        if (logger.isWarnEnabled()) {
          logger.warn("Client already exists with id={}", newClient.getClientId());
        }
        return false;
      }
    }
    clients.add(newClient);
    saveData();
    if (logger.isInfoEnabled()) {
      logger.info("Client added successfully id={}", newClient.getClientId());
    }
    return true;
  }

  /**
   * Removes a client from the data store by clientId.
   *
   * @param clientId the unique identifier of the client to remove
   * @return true if the client was found and removed, false otherwise
   */
  public synchronized boolean removeClient(int clientId) {
    if (clients == null) {
      clients = loadData();
    }
    boolean removed = clients.removeIf(client -> client.getClientId() == clientId);
    if (removed) {
      saveData();
      if (logger.isInfoEnabled()) {
        logger.info("Client removed successfully id={}", clientId);
      }
    } else {
      if (logger.isWarnEnabled()) {
        logger.warn("Client not found for removal id={}", clientId);
      }
    }
    return removed;
  }

  /**
   * Updates an existing client's information.
   *
   * @param updatedClient the {@code Client} object with updated information
   * @return true if the client was found and updated, false otherwise
   */
  public synchronized boolean updateClient(Client updatedClient) {
    if (clients == null) {
      clients = loadData();
    }
    if (updatedClient == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to update with null client");
      }
      return false;
    }
    for (int i = 0; i < clients.size(); i++) {
      if (clients.get(i).getClientId() == updatedClient.getClientId()) {
        clients.set(i, updatedClient);
        saveData();
        if (logger.isInfoEnabled()) {
          logger.info("Client updated successfully id={}", updatedClient.getClientId());
        }
        return true;
      }
    }
    if (logger.isWarnEnabled()) {
      logger.warn("Client not found for update id={}", updatedClient.getClientId());
    }
    return false;
  }

  /**
   * Prints all clients currently stored in the service.
   */
  public synchronized void printClients() {
    if (clients == null) {
      clients = loadData();
    }
    if (logger.isInfoEnabled()) {
      clients.forEach(c -> logger.info("Client: id={} ip={} port={}", 
          c.getClientId(), c.getIpAddress(), c.getPort()));
    }
  }
}
