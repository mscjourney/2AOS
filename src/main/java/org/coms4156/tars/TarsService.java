package org.coms4156.tars;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

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

  private List<User> loadData() {
    try {
      users = mapper.readValue(this.userFile, new TypeReference<List<User>>(){});
    } catch (IOException e) {
      System.err.println("Failed to load users: " + e.getMessage());
      users = new ArrayList<>();
    }
    return users;
  }

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