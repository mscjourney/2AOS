package org.coms4156.tars;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.coms4156.tars.model.User;
import org.coms4156.tars.service.TarsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains the unit tests for the User related functionalities.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserTest { 
  // Currnently directly modifies the data/userPreferences.json
  // Need to create a dummy version for testing so that we don't have to revert each time.
    
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private TarsService tarsService;


  @BeforeEach
  void seedUsers() throws Exception {
    tarsService.loadData();

    ObjectMapper mapper = new ObjectMapper();

    User user1 = new User(1, 1, List.of("sunny"), List.of("70F"), List.of("Boston"));
    User user2 = new User(2, 2,
        List.of("rainy"),
        List.of("60F", "67F"),
        List.of("New York",  "Paris"));

    mockMvc.perform(put("/user/1/add")
        .contentType("application/json")
        .content(mapper.writeValueAsString(user1)))
        // Accept either 200 (added) or 409 (already exists)
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 409) {
            throw new AssertionError("Unexpected status when seeding user 2: " + status);
          }
        });

    mockMvc.perform(put("/user/2/add")
        .contentType("application/json")
        .content(mapper.writeValueAsString(user2)))
        // Accept either 200 (added) or 409 (already exists)
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 409) {
            throw new AssertionError("Unexpected status when seeding user 2: " + status);
          }
        });
  }

  @Test
  public void indexTest() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  @Test
  public void indexTestWithIndexPath() throws Exception {
    this.mockMvc.perform(get("/index"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  @Test
  public void indexTestResponseContentType() throws Exception {
    this.mockMvc.perform(get("/"))
      .andExpect(status().isOk())
      .andExpect(content().contentType("text/plain;charset=UTF-8"))
      .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  @Test // @GetMapping({"/user/{id}"}) Test 1
  public void getUserTest() throws Exception {
    this.mockMvc.perform(get("/user/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(2)))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("60F", "67F")))
      .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));

    this.mockMvc.perform(get("/user/0"))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("User not found.")));
  }

  @Test // @GetMapping({"/user/{id}"}) Test 2
  public void getUserTestWithValidId1() throws Exception {
    this.mockMvc.perform(get("/user/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(1)))
      .andExpect(jsonPath("$.clientId", is(1)))
      .andExpect(jsonPath("$.weatherPreferences", contains("sunny")))
      .andExpect(jsonPath("$.cityPreferences", contains("Boston")));
  }

  @Test // @GetMapping({"/user/{id}"}) Test 3
  public void getUserTestWithNonExistentId() throws Exception {
    this.mockMvc.perform(get("/user/9999"))
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("User not found.")));
  }

  @Test // @PutMapping({"/user/{id}/add"}) Test 1
  public void addUserTest() throws Exception {
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("snowy");

    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("88F");
    temperaturePreferences.add("15C");

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Rome");
    cityPreferences.add("Syndey");
    
    ObjectMapper mapper = new ObjectMapper();

    // Use a unique user ID based on current timestamp to avoid conflicts
    // Uses a placeholder ID for some given client
    int uniqueUserId = (int) (System.currentTimeMillis() % 10000) + 1000;
    User newUser = new User(uniqueUserId, 2, weatherPreferences, temperaturePreferences, 
                              cityPreferences);

    this.mockMvc.perform(put("/user/" + uniqueUserId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(uniqueUserId)))
        .andExpect(jsonPath("$.weatherPreferences", contains("snowy")))
        .andExpect(jsonPath("$.temperaturePreferences", contains("88F", "15C")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey")));

    // Test adding the same user again should fail
    this.mockMvc.perform(put("/user/" + uniqueUserId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("User Id already exists.")));

    // Remove the user to not modify the json file.
    this.mockMvc.perform(put("/user/" + uniqueUserId + "/remove"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("User removed successfully.")));
  }

  @Test // @PutMapping({"/user/{id}/add"}) Test 2
  public void addUserMultipleTimes() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    int userId = (int) (System.currentTimeMillis() % 10000) + 1000;
    User newUser = new User(userId, 10);
    this.mockMvc.perform(put("/user/" + userId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId)))
        .andExpect(jsonPath("$.clientId", is(10)));
    
    this.mockMvc.perform(put("/user/" + userId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("User Id already exists.")));
  }

  @Test // @PutMapping({"/user/{id}/add"}) Test 3
  public void addUserTestWithNullBody() throws Exception {
    // Spring rejects empty body with 400 before reaching controller
    this.mockMvc.perform(put("/user/100/add")
      .contentType("application/json")
      .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test // @PutMapping({"/user/{id}/remove"}) Test 1
  public void removeUserWithNegativeId() throws Exception {
    this.mockMvc.perform(put("/user/-5/remove"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User Id cannot be negative.")));
  }

  @Test // @PutMapping({"/user/{id}/remove"}) Test 2
  public void removeNonExistentUser() throws Exception {
    this.mockMvc.perform(put("/user/" + 123510 + "/remove"))
      .andExpect(status().isConflict())
      .andExpect(content().string(containsString("User removed failed.")));
  }

  @Test // @PutMapping({"/user/{id}/remove"}) Test 3
  public void removeUserMultipleTimes() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    int userId = 10;
    User newUser = new User(userId, 5);

    this.mockMvc.perform(put("/user/" + userId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(userId)))
        .andExpect(jsonPath("$.clientId", is(5)));
    
    this.mockMvc.perform(put("/user/" + userId + "/remove"))
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("User removed successfully.")));

    this.mockMvc.perform(put("/user/" + userId + "/remove"))
      .andExpect(status().isConflict())
      .andExpect(content().string(containsString("User removed failed.")));
  }

  @Test
  public void getUserList() throws Exception {
    this.mockMvc.perform(get("/userList")).andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].clientId", is(1)))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("sunny")))
        .andExpect(jsonPath("$[1].id", is(2)))
        .andExpect(jsonPath("$[1].clientId", is(2)))
        .andExpect(jsonPath("$[1].weatherPreferences", contains("rainy")));
  }

  @Test
  public void getClientUserList() throws Exception {
    this.mockMvc.perform(get("/userList/client/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id", is(1)))
        .andExpect(jsonPath("$[0].clientId", is(1)))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("sunny")));
    
    this.mockMvc.perform(get("/userList/client/2")).andDo(print())
        .andExpect(jsonPath("$[0].id", is(2)))
        .andExpect(jsonPath("$[0].clientId", is(2)))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("rainy")));
  }
}