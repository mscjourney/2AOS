package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This class defines the Tars Service and the json file used to store users' preferences.
 * Defines methods to load and save data from and to the file.
 * Load data is done during initialization of the service.
 * Load from resource file path: data/userPreferences.json
 */
@Service
public class TarsService {

  private static final Logger logger = LoggerFactory.getLogger(TarsService.class);
  private final File userFile;
  private final ObjectMapper mapper = new ObjectMapper();
  private List<User> users;

  /**
   * Constructor with path injected from application properties.
   * If the property 'tars.data.path' is not set,
   * defaults to ./data/userPreferences.json (writable location)
   */
  public TarsService(
      @Value("${tars.data.path:./data/userPreferences.json}") String userFilePath) {
    this.userFile = new File(userFilePath);
    if (!userFile.exists()) {
      try {
        File parent = userFile.getParentFile();
        if (parent != null) {
          parent.mkdirs();
        }
        mapper.writeValue(userFile, new ArrayList<User>());
        if (logger.isInfoEnabled()) {
          logger.info("Created new user preferences file at: {}", 
            userFile.getAbsolutePath());
        }
      } catch (IOException e) {
        if (logger.isErrorEnabled()) {
          logger.error("Failed to create user preferences file: {}", userFilePath, e);
        }
      }
    } else {
      if (logger.isInfoEnabled()) {
        logger.info("Using existing user preferences file at: {}", 
          userFile.getAbsolutePath());
      }
    }
    this.users = loadData();
  }

  /**
   *  Loads the existing user preferences data from USER_FILENAME.
   */
  private List<User> loadData() {
    try {
      return mapper.readValue(this.userFile, new TypeReference<List<User>>() {});
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to load users from {}", userFile.getPath(), e);
      }
      return new ArrayList<>();
    }
  }

  /**
   *  Writes the current list of user preferences stored in the user list to the json file.
   */
  public synchronized void saveData() {
    try {
      mapper.writeValue(this.userFile, users);
    } catch (IOException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Failed to write users to {}", userFile.getPath(), e);
      }
    }
  }

  /**
   * Returns a defensive copy of the list of users stored in the service.
   *
   * @return a List of User objects representing all users stored in the service
   */
  public synchronized List<User> getUserList() {
    if (users == null) {
      users = loadData();
    }
    return new ArrayList<>(users);  // Return defensive copy
  }

  /**
   * Updates the json file by writing the preference data of the new user into the file.
   *
   * @param newUser the {@code User} object containing the preferences to be added to the file
   * @return If the new user was successfully added to the file, returns true.
   *         If the id of the new user already existed, we do not write the user data to the file
   *         and return false.
   */
  public synchronized boolean addUser(User newUser) {
    if (users == null) {
      users = loadData();
    }
    if (newUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to add null user");
      }
      return false;
    }
    for (User user : users) {
      if (user.equals(newUser)) {
        return false;
      }
    }
    users.add(newUser);
    saveData();
    return true;
  }
  
  /**
   * Finds the user from the list based on the user id.
   *
   * @param userId the id of the user
   * @return If the user specified by userId exists, returns the {@code User} object containing
   *         the user's preferences. If no such user exists, returns null.
   */
  public synchronized User getUser(int userId) {
    if (users == null) {
      users = loadData();
    }

    if (userId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("User Id cannot be negative");
      }
      return null;
    }
    
    for (User user : users) {
      if (user.getId() == userId) {
        return user;
      }
    }

    return null;
  }

  /**
   * Prints all users currently stored in the service.
   */
  public synchronized void printUsers() {
    if (users == null) {
      users = loadData();
    }
    if (logger.isInfoEnabled()) {
      users.forEach(u -> logger.info("User: {}", u));
    }
  }
}