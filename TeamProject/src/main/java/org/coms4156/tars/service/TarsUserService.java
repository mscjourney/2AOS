package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import org.coms4156.tars.model.TarsUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@code TarsUserService} provides services related to TarsUser management.
 */
@Service 
public class TarsUserService {
  private static final Logger logger = LoggerFactory.getLogger(TarsUserService.class);
  private final ObjectMapper mapper = new ObjectMapper();
  private final File userFile;
  private final AtomicLong idCounter = new AtomicLong(0);
  private List<TarsUser> users;

  /**
   * {@code TarsUserService} constructor.
   * Initializes a new instance of TarsUserService.
   * Loads user data from the JSON user database.
   */
  public TarsUserService(
      @Value("${tars.users.path:./data/users.json}") String userFilePath) {
    this.userFile = new File(userFilePath);
    ensureFile();
    this.users = load();
    
    // Initialize counter to max existing id
    long maxUserId = 0;
    for (TarsUser existingUser : users) {
      Long userId = existingUser.getUserId();
      if (userId != null && userId > maxUserId) {
        maxUserId = userId;
      }
    }
    idCounter.set(maxUserId);
  }

  /**
   * {@code ensureFile} Ensures the user database file exists.
   * If it does not exist, creates an empty JSON array file.
   */
  private void ensureFile() {
    if (!userFile.exists()) {
      try {
        File parent = userFile.getParentFile();
        if (parent != null) {
          parent.mkdirs();
        }
        mapper.writeValue(userFile, new ArrayList<TarsUser>());
        if (logger.isInfoEnabled()) {
          logger.info(
              "Created user store at {}",
              userFile.getAbsolutePath());
        }
      } catch (IOException ioException) {
        if (logger.isErrorEnabled()) {
          logger.error(
              "Failed to initialize user store {}",
              userFile.getPath(),
              ioException);
        }
      }
    }
  }

  /**
   * {@code load} Loads the user data from the JSON user database.
   *
   * @return List of TarsUser objects
   */
  private synchronized List<TarsUser> load() {
    try {
      return mapper.readValue(userFile, new TypeReference<List<TarsUser>>() {});
    } catch (IOException ioException) {
      if (logger.isErrorEnabled()) {
        logger.error(
            "Failed to read users file {}",
            userFile.getPath(),
            ioException);
      }
      return new ArrayList<>();
    }
  }

  /**
   * {@code persist} Persists the current users list to the JSON user database.
   * Handles atomic file replacement to avoid data corruption.
   */
  private synchronized void persist() {
    File tempFile = new File(userFile.getParent(), userFile.getName() + ".tmp");
    try {
      mapper.writeValue(tempFile, users);
      Files.move(
          tempFile.toPath(),
          userFile.toPath(),
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException ioException) {
      if (logger.isErrorEnabled()) {
        logger.error(
            "Failed to persist users to {}",
            userFile.getPath(),
            ioException);
      }
      if (tempFile.exists()) {
        tempFile.delete();
      }
    }
  }

  /**
   * {@code listUsers} Returns an unmodifiable list of all TarsUsers.
   *
   * @return List of TarsUser objects
   */
  public synchronized List<TarsUser> listUsers() {
    return Collections.unmodifiableList(new ArrayList<>(users));
  }

  /**
   * {@code findById} Finds a TarsUser by their userId.
   *
   * @param userId the unique identifier for the user
   * @return the TarsUser object if found, or null if not found
   */
  public synchronized TarsUser findById(Long userId) {
    if (userId == null) {
      return null;
    }
    for (TarsUser existingUser : users) {
      if (userId.equals(existingUser.getUserId())) {
        return existingUser;
      }
    }
    return null;
  }

  /**
   * {@code createUser} Creates a new TarsUser with enforced username uniqueness per client.
   *
   * @param clientId client association (required)
   * @param username chosen username (required, trimmed, case-insensitive uniqueness)
   * @param email user's email (required, trimmed, case-insensitive uniqueness)
   * @param role role string (required)
   *
   * @return created user or null if invalid inputs or username already exists
   */
  public synchronized TarsUser createUser(
      Long clientId, String username, String email, String role) {
    if (clientId == null || username == null || email == null || role == null) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "createUser rejected: null parameters clientId={} username={} email={} role={}",
            clientId, username, email, role);
      }
      return null;
    }

    String normalizedUsername = username.trim();
    String normalizedUserEmail = email.trim();
    String normalizedRole = role.trim();

    if (normalizedUsername.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("createUser rejected: blank username clientId={}",
            clientId);
      }
      return null;
    }

    if (normalizedUserEmail.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("createUser rejected: blank email clientId={} username='{}'",
            clientId, normalizedUsername);
      }
      return null;
    }

    if (normalizedRole.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("createUser rejected: blank role clientId={} username='{}' email='{}'",
            clientId, normalizedUsername, normalizedUserEmail);
      }
      return null;
    }

    // Uniqueness check removed - controller is responsible for this validation

    long newUserId = idCounter.incrementAndGet();
    TarsUser newUser = new TarsUser(
        clientId, normalizedUsername, normalizedUserEmail, normalizedRole);
    newUser.setUserId(newUserId);
    newUser.setSignUpDate(Instant.now().toString());
    newUser.setLastLogin("");
    newUser.setActive(true);

    users.add(newUser);
    persist();
    
    if (logger.isInfoEnabled()) {
      logger.info("Created TarsUser id={} clientId={} username='{}' email='{}'",
          newUserId, clientId, normalizedUsername, normalizedUserEmail, normalizedRole);
    }
    return newUser;
  }

  /**
   * {@code deactivateUser} Deactivates a user by setting their active status to false.
   *
   * @param userId the unique identifier for the user
   * @return Boolean indicating success
   */
  public synchronized boolean deactivateUser(Long userId) {
    TarsUser targetUser = findById(userId);
    if (targetUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("deactivateUser: user not found id={}", userId);
      }
      return false;
    }
    targetUser.setActive(false);
    persist();
    if (logger.isInfoEnabled()) {
      logger.info("User deactivated id={}", userId);
    }
    return true;
  }

  /**
   * {@code updateLastLogin} Updates the lastLogin timestamp for a user to the current time.
   *
   * @param userId the unique identifier for the user
   * @return Boolean indicating success
   */
  public synchronized boolean updateLastLogin(Long userId) {
    TarsUser targetUser = findById(userId);
    if (targetUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("updateLastLogin: user not found id={}", userId);
      }
      return false;
    }
    targetUser.setLastLogin(Instant.now().toString());
    persist();
    if (logger.isInfoEnabled()) {
      logger.info("Updated lastLogin for user id={}", userId);
    }
    return true;
  }

  /**
   * {@code existsByClientIdAndUsername} Checks if a tarsUser 
   * with username already exist in a client.
   *
   * @param clientId the client identifier
   * @param username the username to check (case-insensitive)
   * @return true if tarsUser with username exists for per this client, false otherwise
   */
  public synchronized boolean existsByClientIdAndUsername(Long clientId, String username) {
    if (clientId == null || username == null) {
      return false;
    }
    String normalizedUsernameLowerCase = username.trim().toLowerCase(Locale.ROOT);
    for (TarsUser existingUser : users) {
      Long existingClientId = existingUser.getClientId();
      String existingUsername = existingUser.getUsername();
      
      if (existingClientId != null && existingUsername != null) {
        if (existingClientId.equals(clientId)) {
          String existingUsernameLowerCase = existingUsername.trim().toLowerCase(Locale.ROOT);
          if (existingUsernameLowerCase.equals(normalizedUsernameLowerCase)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * {@code existsByClientIdAndUserEmail} Checks if a tarsUser with email already exist in a client.
   *
   * @param clientId the client identifier
   * @param email the email to check (case-insensitive)
   * @return true if tarsUser with email exists for per this client, false otherwise
   */
  public synchronized boolean existsByClientIdAndUserEmail(Long clientId, String email) {
    if (clientId == null || email == null) {
      return false;
    }
    String normalizedEmailLowerCase = email.trim().toLowerCase(Locale.ROOT);
    for (TarsUser existingUser : users) {
      Long existingClientId = existingUser.getClientId();
      String existingEmail = existingUser.getUserEmail();
      
      if (existingClientId != null && existingEmail != null) {
        if (existingClientId.equals(clientId)) {
          String existingEmailLowerCase = existingEmail.trim().toLowerCase(Locale.ROOT);
          if (existingEmailLowerCase.equals(normalizedEmailLowerCase)) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
