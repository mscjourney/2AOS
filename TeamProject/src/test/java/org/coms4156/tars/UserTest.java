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
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.UserPreference;
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

  ObjectMapper mapper = new ObjectMapper();
  /**
   * {@code seedUsers} Seeds initial test users before each test execution.
   *
   * @throws Exception if user seeding fails
   */
  @BeforeEach
  void seedUsers() throws Exception {
    tarsService.loadData();
    UserPreference user1 = new UserPreference(1L,
        List.of("sunny"), List.of("70F"), List.of("Boston"));

    UserPreference user2 = new UserPreference(2L,
        List.of("rainy"),
        List.of("60F", "67F"),
        List.of("New York",  "Paris"));

    mockMvc.perform(put("/setPreference/1")
        .contentType("application/json")
        .content(mapper.writeValueAsString(user1)))
        // Accept either 200 (added) or 400 (RequestBody is null) 
        // or 404 (TarsUser could not be found)
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 400 && status != 404) {
            throw new AssertionError("Unexpected status when seeding user 2: " + status);
          }
        });

    mockMvc.perform(put("/setPreference/2")
        .contentType("application/json")
        .content(mapper.writeValueAsString(user2)))
        // Accept either 200 (added) or 400 (RequestBody is null) 
        // or 404 (TarsUser could not be found)
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 400 && status != 404) {
            throw new AssertionError("Unexpected status when seeding user 2: " + status);
          }
        });
  }

  /**
   * {@code indexTest} Tests the index endpoint returns the welcome message.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void indexTest() throws Exception {
    mockMvc.perform(get("/"))
      .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  /**
   * {@code indexTestWithIndexPath} Tests the /index endpoint returns the welcome message.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void indexTestWithIndexPath() throws Exception {
    mockMvc.perform(get("/index"))
      .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  /**
   * {@code indexTestResponseContentType} Tests that the index endpoint returns
   * the correct content type.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void indexTestResponseContentType() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  /**
   * {@code getUserTest} Tests retrieving a user by ID and validates user data.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void getUserPreferenceTestValidId() throws Exception {
    mockMvc.perform(get("/retrievePreference/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(2))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("60F", "67F")))
        .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));
  }

  /**
   * {@code getUserTestWithValidId} Tests retrieving user with ID 1 and
   * validates their preferences.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void getUserPreferenceTestWithNoPreferences() throws Exception {
    mockMvc.perform(get("/retrievePreference/3"))
      .andExpect(status().isBadRequest())
      .andExpect(content().string("User had no existing preferences."));
  }

  /**
   * {@code getUserTestWithNonExistentId} Tests that requesting preferences of a non-existent TarsUser.
   *
   * @throws Exception if the request fails
   */
  @Test
  public void getUserPreferenceTestWithNoTarsUser() throws Exception {
    mockMvc.perform(get("/retrievePreference/9999"))
      .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("TarsUser not found.")));
  }

  @Test
  public void setUserPreferenceTestValidId() throws Exception {
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("snowy");

    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("88F");
    temperaturePreferences.add("15C");

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Rome");
    cityPreferences.add("Syndey");
    
    UserPreference newUserPreference = 
        new UserPreference(1L, weatherPreferences, temperaturePreferences, cityPreferences);
    mockMvc.perform(put("/setPreference/1")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUserPreference)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.weatherPreferences", contains("snowy")))
        .andExpect(jsonPath("$.temperaturePreferences", contains("88F", "15C")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey")));

    weatherPreferences.add("sunny");
    temperaturePreferences.add("60F");
    cityPreferences.add("Venice");

    // Should be able to setPreferences for same User multiple times
    UserPreference modifiedUserPreference = 
        new UserPreference(1L, weatherPreferences, temperaturePreferences, cityPreferences);
    mockMvc.perform(put("/setPreference/1")
      .contentType("application/json")
      .content(mapper.writeValueAsString(modifiedUserPreference)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.weatherPreferences", contains("snowy", "sunny")))
        .andExpect(jsonPath("$.temperaturePreferences", contains("88F", "15C", "60F")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey", "Venice")));
  }

  @Test
  public void setUserPreferenceWithNoTarsUser() throws Exception {
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = new UserPreference(15L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/15")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));
  }

  @Test
  public void setUserPreferenceNegativeId() throws Exception { 
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = new UserPreference(-1L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/-1")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));
  }

  @Test
  public void setUserPreferenceMismatchId() throws Exception {
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = new UserPreference(5L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/10")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Path Variable and RequestBody User Id do not match."));
  }

  @Test
  public void clearPreferenceValidId() throws Exception {
    mockMvc.perform(put("/clearPreference/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("User Preference cleared successfully."));

    // Cannot clear a preference that was already cleared
    mockMvc.perform(put("/clearPreference/1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User had no existing preferences."));
  }

  @Test
  public void clearPreferenceNoTarsUser() throws Exception {
    mockMvc.perform(put("/clearPreference/10"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));   
  }

  @Test
  public void clearPreferenceNegativeId() throws Exception {
    mockMvc.perform(put("/clearPreference/-4"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));   
  }

  @Test
  public void testGetUserPreferenceList() throws Exception {
    mockMvc.perform(get("/userPreferenceList"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("sunny")))
        .andExpect(jsonPath("$[0].temperaturePreferences", contains("70F")))
        .andExpect(jsonPath("$[0].cityPreferences", contains("Boston")))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].weatherPreferences", contains("rainy")))
        .andExpect(jsonPath("$[1].temperaturePreferences", contains("60F", "67F")))
        .andExpect(jsonPath("$[1].cityPreferences", contains("New York", "Paris")));
  }
}