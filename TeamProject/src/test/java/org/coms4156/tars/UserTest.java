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


  @BeforeEach
  void seedUsers() throws Exception {
    ObjectMapper mapper = new ObjectMapper();

    // Seed user 2 (ignore if already exists)
    User user2 = new User(2,
        List.of("rainy"),
        List.of("60F", "67F"),
        List.of("New York",  "Paris"));

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
    this.mockMvc.perform(get("/")).andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  @Test
  public void getUserTest() throws Exception {
    this.mockMvc.perform(get("/user/2")).andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(2)))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("60F", "67F")))
      .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));

    this.mockMvc.perform(get("/user/0")).andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("User not found.")));
  }

  @Test
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
    int uniqueUserId = (int) (System.currentTimeMillis() % 10000) + 1000;
    User newUser = new User(uniqueUserId, weatherPreferences, temperaturePreferences, 
                              cityPreferences);

    this.mockMvc.perform(put("/user/" + uniqueUserId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(uniqueUserId)))
        .andExpect(jsonPath("$.weatherPreferences", contains("snowy")))
        .andExpect(jsonPath("$.temperaturePreferences", contains("88F", "15C")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey")));

    // Test adding the same user again should fail
    this.mockMvc.perform(put("/user/" + uniqueUserId + "/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("User Id already exists.")));
  }
}