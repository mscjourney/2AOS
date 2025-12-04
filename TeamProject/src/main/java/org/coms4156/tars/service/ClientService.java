package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
  private final FileMover fileMover;

  /**
   * {@code FileMover} Strategy interface for file moves (test injection).
   */
  public interface FileMover {
    void move(Path source, Path target, CopyOption... options) throws IOException;
  }

  private static final FileMover DEFAULT_FILE_MOVER =
      (src, dest, opts) -> java.nio.file.Files.move(src, dest, opts);

  /**
   * Constructor with path injected from application properties.
   * If the property 'tars.client.data.path' is not set,
   * defaults to ./data/ClientData.json (writable location).
   *
   * @param clientFilePath the path to the client data JSON file
   * @param fileMover the file mover strategy (null uses default)
   */
  @Autowired
  public ClientService(
      @Value("${tars.client.data.path:./data/clients.json}") String clientFilePath,
      FileMover fileMover) {
    this.clientFile = new File(clientFilePath);
    this.fileMover = fileMover == null ? DEFAULT_FILE_MOVER : fileMover;
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
   * Find a client by its API key.
   * Returns null if not found or if apiKey is null/blank.
   */
  public synchronized Client findByApiKey(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      return null;
    }
    for (Client c : clients) {
      if (c != null && apiKey.equals(c.getApiKey())) {
        return c;
      }
    }
    return null;
  }
  /**
   * {@code generateApiKey} Generates a new API key for a client.
   *
   * @return the generated API key as a String
   */
  private String generateApiKey() {
    // Simple random 32-char hex token
    byte[] bytes = new byte[16];
    new java.security.SecureRandom().nextBytes(bytes);
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  /**
   * {@code rotateApiKey} Rotates the API key for a given client.
   *
   * @param clientId the ID of the client whose API key is to be rotated
   * @return the new API key, or null if client not found
   */
  public synchronized String rotateApiKey(long clientId) {
    // Operate on internal reference (not the defensive copy)
    Client target = null;
    for (Client c : clients) {
      if (c.getClientId() == clientId) {
        target = c;
        break;
      }
    }
    if (target == null) {
      logger.warn("rotateApiKey: client not found id={}", clientId);
      return null;
    }
    String newKey = generateApiKey();
    target.setApiKey(newKey);
    saveData();
    if (logger.isInfoEnabled()) {
      logger.info("API key rotated for client id={} (key suffix={})",
          clientId, newKey.substring(newKey.length() - 4));
    }
    return newKey;
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
   * {@code saveData} Writes the current list of clients to the JSON file.
   */
  public synchronized void saveData() {
    File tempFile = new File(clientFile.getParent(), clientFile.getName() + ".tmp");
    try {
      mapper.writeValue(tempFile, clients);
      try {
        fileMover.move(
            tempFile.toPath(),
            clientFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
      } catch (java.nio.file.AtomicMoveNotSupportedException atomicEx) {
        fileMover.move(
            tempFile.toPath(),
            clientFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING);
        if (logger.isWarnEnabled()) {
          logger.warn("Atomic move not supported for clients file; used non-atomic fallback.");
        }
      }
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to write clients to {}", clientFile.getPath(), e);
      }
      if (tempFile.exists()) {
        tempFile.delete();
      }
    }
  }

  /**
   * {@code getClientList} Returns a defensive copy of the list of clients stored in the service.
   *
   * @return a List of {@code Client} objects representing all clients
   */
  public synchronized List<Client> getClientList() {
    if (clients == null) {
      clients = loadData();
    }
    List<Client> snapshot = new ArrayList<>();
    for (Client c : clients) {
      snapshot.add(copyClient(c));
    }
    return snapshot;
  }

  /**
   * {@code getClient} Retrieves a {@code Client} by its clientId.
   *
   * @param clientId the unique identifier for the client
   * @return the {@code Client} object if found, or null if not found
   */
  public synchronized Client getClient(long clientId) {
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
        return copyClient(client);
      }
    }
    return null;
  }

  /**
   * {@code setRateLimit} Updates the per-minute rate limit for a client and persists the change.
   *
   * @param clientId the client identifier
   * @param limit the new limit, must be positive
   * @return updated Client copy or null if not found/invalid
   */
  public synchronized Client setRateLimit(long clientId, int limit) {
    if (limit <= 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("setRateLimit: invalid limit {} for client {}", limit, clientId);
      }
      return null;
    }
    if (clients == null) {
      clients = loadData();
    }
    for (Client c : clients) {
      if (c.getClientId() != null && c.getClientId() == clientId) {
        c.setRateLimitPerMinute(limit);
        saveData();
        if (logger.isInfoEnabled()) {
          logger.info("Rate limit updated for client id={} limit={}", clientId, limit);
        }
        return copyClient(c);
      }
    }
    if (logger.isWarnEnabled()) {
      logger.warn("setRateLimit: client not found id={}", clientId);
    }
    return null;
  }

  /**
   * {@code getNextClientId} Generates the next 
   * available client ID based on the highest existing ID.
   *
   * @return the next available client ID (starts at 1 if no clients exist)
   */
  private synchronized long getNextClientId() {
    if (clients == null) {
      clients = loadData();
    }
    
    long maxId = 0;
    for (Client client : clients) {
      if (client.getClientId() > maxId) {
        maxId = client.getClientId();
      }
    }
    
    long nextId = maxId + 1;
    if (logger.isDebugEnabled()) {
      logger.debug("Generated next client ID: {}", nextId);
    }
    return nextId;
  }

  /**
   * {@code uniqueNameCheck} Checks if a client name is unique.
   *
   * @param newClientName the client name to check for uniqueness; comparison is case-insensitive.
   * @return true if the client name is unique, false otherwise
   */
  public synchronized boolean uniqueNameCheck(String newClientName) {
    if (newClientName == null) {
      return false;
    }
    for (Client client : clients) {
      String existing = client.getName();
      if (existing != null && existing.equalsIgnoreCase(newClientName)) {
        if (logger.isWarnEnabled()) {
          logger.warn("Client name already exists: {}", newClientName);
        }
        return false;
      }
    }
    return true;
  }
  
  /**
   * {@code uniqueEmailCheck} Checks if a client email is unique.
   *
   * @param email the client email to check for uniqueness; comparison is case-insensitive.
   * @return true if the client email is unique, false otherwise
   */
  public synchronized boolean uniqueEmailCheck(String email) {
    if (email == null) {
      return false;
    }
    for (Client client : clients) {
      String existing = client.getEmail();
      if (existing != null && existing.equalsIgnoreCase(email)) {
        if (logger.isWarnEnabled()) {
          logger.warn("Client email already exists: {}", email);
        }
        return false;
      }
    }
    return true;
  }

  /**
   * {@code createClient} Creates a new client with an auto-generated ID.
   * The client ID is automatically assigned based on the last used ID in the datastore.
   *
   * @param name the name of the client. Ensure it's unique.
   * @param email the email address of the client. Ensure it's unique.
   * @return the created {@code Client} object with assigned ID
   */
  public synchronized Client createClient(String name, String email) {
    if (name == null || name.isBlank()) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to create client with blank name");
      }
      return null;
    }

    if (email == null || email.isBlank()) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to create client with blank email");
      }
      return null;
    }


    // Check name uniqueness
    if (!uniqueNameCheck(name)) {
      return null;
    }

    // Check email uniqueness
    if (!uniqueEmailCheck(email)) {
      return null;
    }

    long newId = getNextClientId();
    Client client = new Client();
    client.setClientId(newId);
    client.setName(name);
    client.setEmail(email);
    client.setApiKey(generateApiKey());
    clients.add(client);
    saveData();
    if (logger.isInfoEnabled()) {
      logger.info("Client created successfully id={} name={} email={}", newId, name, email);
    }
    return client;
  }

  /**
   * {@code removeClient} Removes a client from the data store by clientId.
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
   * {@code updateClient} Updates an existing client's information.
   *
   * @param updatedClient the {@code Client} object with updated information
   * @return true if the client was found and updated, false otherwise
   */
  public synchronized boolean updateClient(Client updatedClient) {
    if (updatedClient == null) {
      logger.warn("Attempted to update with null client");
      return false;
    }
    Long updatedId = updatedClient.getClientId();
    if (updatedId == null) {
      logger.warn("Updated client missing clientId");
      return false;
    }
    String newName = updatedClient.getName();
    if (newName == null || newName.isBlank()) {
      logger.warn("Attempted to update client id={} with blank name", updatedId);
      return false;
    }
    // Explicit uniqueness scan excluding the target id (handles mutated reference)
    for (Client other : clients) {
      if (other.getClientId() != null
          && !other.getClientId().equals(updatedId)
          && other.getName() != null
          && other.getName().equalsIgnoreCase(newName)) {
        logger.warn("Name '{}' already in use. Update aborted.", newName);
        return false;
      }
    }
    // Locate and update target
    for (int i = 0; i < clients.size(); i++) {
      Client existing = clients.get(i);
      if (existing.getClientId() != null && existing.getClientId().equals(updatedId)) {
        if (updatedClient.getApiKey() == null) {
          updatedClient.setApiKey(existing.getApiKey());
        }
        // Store a copy internally to avoid external references lingering
        clients.set(i, copyClient(updatedClient));
        saveData();
        logger.info("Client updated successfully id={}", updatedId);
        return true;
      }
    }
    logger.warn("Client not found for update id={}", updatedId);
    return false;
  }

  /**
   * {@code printClients} Logs all clients at info level.
   */
  public synchronized void printClients() {
    for (Client client : clients) {
      if (logger.isInfoEnabled()) {
        logger.info("Client: id={} name={} email={} rateLimit={} concurrent={}",
            client.getClientId(), client.getName(), client.getEmail(),
            client.getRateLimitPerMinute(),
            client.getMaxConcurrentRequests());
      }
    }
  }

  // ==================== Internal Helpers ====================
  private Client copyClient(Client source) {
    if (source == null) {
      return null;
    }
    Client copy = new Client();
    copy.setClientId(source.getClientId());
    copy.setName(source.getName());
    copy.setEmail(source.getEmail());
    copy.setApiKey(source.getApiKey());
    copy.setRateLimitPerMinute(source.getRateLimitPerMinute());
    copy.setMaxConcurrentRequests(source.getMaxConcurrentRequests());
    return copy;
  }
}
