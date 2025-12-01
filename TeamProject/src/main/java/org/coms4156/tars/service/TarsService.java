package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.coms4156.tars.model.UserPreference;
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
  private List<UserPreference> users;

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
        mapper.writeValue(userFile, new ArrayList<UserPreference>());
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
  public List<UserPreference> loadData() {
    try {
      return mapper.readValue(this.userFile, new TypeReference<List<UserPreference>>() {});
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
  public synchronized List<UserPreference> getUserPreferenceList() {
    if (users == null) {
      users = loadData();
    }
    List<UserPreference> copy = new ArrayList<>(users);
    // Sort deterministically by id (null ids last to avoid NPE in comparator)
    copy.sort(Comparator.comparing(UserPreference::getId, Comparator.nullsLast(Long::compareTo)));
    return copy;  // Return defensive, sorted copy
  }

  /**
   * Updates the json file by writing the preference data of the new user into the file.
   *
   * @param newUserPreference the {@code UserPreference} object containing the preferences to 
   *                          be added to the file
   * @return If the new user was successfully added to the file, returns true.
   *         If the id of the new user already existed, we overwrite the preferences data,
   *         and return. Return false if the userPreferences pass in is null.
   */
  public synchronized boolean setUserPreference(UserPreference newUserPreference) {
    if (users == null) {
      users = loadData();
    }
  
    if (newUserPreference == null || newUserPreference.getId() == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("User Body or User Id is null");
      }
      return false;
    }
    
    // If userPreference already exists, modify the entry.
    for (int i = 0; i < users.size(); i++) {
      if (users.get(i).getId().equals(newUserPreference.getId())) {
        users.set(i, newUserPreference);
        saveData();
        return true;
      }
    }


    users.add(newUserPreference);
    saveData();
    return true;
  }
  
  /**
   * Updates the json file by clearing the preference data of an existing user.
   *
   * @param userId the id of the user whose data we want to remove
   * @return If the existing user was successfully cleared in the file, returns true.
   *         If the userId specified does not exist or is an invalid userId (< 0), returns false.
   */
  public synchronized boolean clearPreference(Long userId) {
    if (users == null) {
      users = loadData();
    }
    
    if (userId == null || userId < 0x0) {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid User Id (negative or null) was passed in");
      }
      return false;
    }

    UserPreference toRemove = null;
    for (UserPreference user : users) {
      if (user.getId().equals(userId)) {
        toRemove = user;
        break;
      }
    }
    if (toRemove != null) {
      users.remove(toRemove);
      saveData();
      return true;
    }

    if (logger.isWarnEnabled()) {
      logger.warn("User Preference with id {} could not be found", userId);
    }
    return false;
  }

  /**
   * Updates the preferences of an existing user in the json file.
   *
   * @param updatedUser the {@code UserPreference} object containing the updated preferences
   * @return If the user was successfully updated, returns true.
   *         If the user does not exist or updatedUser is null, returns false.
   */
  public synchronized boolean updateUser(UserPreference updatedUser) {
    if (users == null) {
      users = loadData();
    }
    
    if (updatedUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to update null user");
      }
      return false;
    }
    
    if (updatedUser.getId() < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("Attempted to update user with negative id");
      }
      return false;
    }
    
    for (int i = 0; i < users.size(); i++) {
      UserPreference user = users.get(i);
      if (user.getId() == updatedUser.getId()) {
        // Update the user's preferences
        user.setWeatherPreferences(updatedUser.getWeatherPreferences());
        user.setTemperaturePreferences(updatedUser.getTemperaturePreferences());
        user.setCityPreferences(updatedUser.getCityPreferences());
        saveData();
        if (logger.isInfoEnabled()) {
          logger.info("Updated user preferences for userId={}", updatedUser.getId());
        }
        return true;
      }
    }
    
    if (logger.isWarnEnabled()) {
      logger.warn("Attempted to update non-existing user with id {}", updatedUser.getId());
    }
    return false;
  }

  /**
   * Finds the user from the list based on the user id.
   *
   * @param userId the id of the user
   * @return If the user specified by userId exists, returns the
   *         {@code UserPreference} object containing the user's preferences.
   *         If no such user exists, returns null.
   */
  public synchronized UserPreference getUserPreference(Long userId) {
    if (users == null) {
      users = loadData();
    }

    if (userId == null || userId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid User Id (negative or null) was passed in");
      }
      return null;
    }
    
    for (UserPreference user : users) {
      if (user.getId().equals(userId)) {
        return user;
      }
    }

    return null;
  }
}