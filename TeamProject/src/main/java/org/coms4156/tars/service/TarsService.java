package org.coms4156.tars.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.User;
import java.io.IOException;
import org.springframework.stereotype.Service;

/**
 *  This class defines the Tars Service and the json file used to store users' preferences.
 *  Defines methods to load and save data from and to the file.
 */
@Service
public class TarsService {
  private static final String USER_FILENAME = "data/userPreferences.json";
  private final File userFile;
  private final ObjectMapper mapper = new ObjectMapper();
  private List<User> users;

  public TarsService() {
    this.userFile = new File(USER_FILENAME);
    this.users = loadData();
  }

  /**
   *  Loads the existing user preferences data from USER_FILENAME.
   */
  private List<User> loadData() {
    try {
      users = mapper.readValue(this.userFile, new TypeReference<List<User>>(){});
    } catch (IOException e) {
      System.err.println("Failed to load users: " + e.getMessage());
      users = new ArrayList<>();
    }
    return users;
  }

  /**
   *  Writes the current list of user preferences stored in the user list to the json file.
   */
  public void saveData() {
    try {
      mapper.writeValue(this.userFile, users);
    } catch (IOException e) {
      System.err.println("Failed to write users: " + e.getMessage());
    }
  }

  public List<User> getUsers() {
    return users;
  }

  /**
   * Updates the json file by writing the preference data of the new user into the file.
   *
   * @param newUser the {@code User} object containing the preferences to be added to the file
   * @return If the new user was successfully added to the file, returns true.
   *         If the id of the new user already existed, we do not write the user data to the file
   *         and return false.
   */
  public boolean addUser(User newUser) {
    for (User user : this.getUsers()) {
      // Make sure that no user with the same id already exists.
      if (user.equals(newUser)) {
        return false;
      }
    }
    users.add(newUser);
    saveData();
    return true;
  }
  
  public void printUsers() {
    users.forEach(System.out::println);
  }
}