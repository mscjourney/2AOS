package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.util.LoggerTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code UserPreferenceEndpointTest} 
 * Contains Equivalence Partitions testing for UserPreference related endpoints.
 * Covers:
 *    GET /retrievePreference/{id}
 *    PUT /setPreference/{id}
 *    PUT /clearPreference/{id}
 *    GET /userPreferenceList
 *    GET /userPreferenceList/client/{clientId}
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserPreferenceEndpointTest {
    
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private TarsService tarsService;

  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * {@code seedUsers} Seeds initial test users before each test execution.
   *
   * @throws Exception if user seeding fails
   */
  @BeforeEach
  void seedUsers() throws Exception {
    tarsService.loadData();
    UserPreference user1 = new UserPreference(1L,
        List.of("sunny"), List.of("22"), List.of("Boston"));

    UserPreference user2 = new UserPreference(2L,
        List.of("rainy"),
        List.of("15", "19"),
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

  /* ======= /retrievePreference/{id} Equivalence Partitions ======= */

  /**
   * {@code getUserPreferenceTestValidId}
   *  Equivalence Partition 1: id is non-negative, there is a TarsUser 
   *    associated with that id, and userPreference for that TarsUser has been set.
   */
  @Test
  public void getUserPreferenceTestValidId() throws Exception {
    mockMvc.perform(get("/retrievePreference/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(1))
      .andExpect(jsonPath("$.weatherPreferences", contains("sunny")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("22")))
        .andExpect(jsonPath("$.cityPreferences", contains("Boston")));
    
    mockMvc.perform(get("/retrievePreference/2"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(2))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("15", "19")))
        .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));
  }

  /**
   * {@code getUserPreferenceTestWithNoPreferences}
   * Equivalence Partition 2: id is non-negative, there is a TarsUser 
   *    associated with that id, but no userPreference has been previously set.
   */
  @Test
  public void getUserPreferenceTestWithNoPreferences() throws Exception {
    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isOk());
    mockMvc.perform(get("/retrievePreference/2")) // No longer has any preferences
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User had no existing preferences."));
  
    // TarsUser with id 3 exist but no preferences have been set.
    mockMvc.perform(get("/retrievePreference/3"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User had no existing preferences."));
  }

  /**
   * {@code getUserPreferenceTestWithNoTarsUse}
   * Equivalence Partition 3: id is non-negative, but there is no TarsUser
   *    associated with that id.
   */
  @Test
  public void getUserPreferenceTestWithNoTarsUser() throws Exception {
    mockMvc.perform(get("/retrievePreference/0"))
      .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("TarsUser not found.")));
  
    mockMvc.perform(get("/retrievePreference/9999"))
      .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("TarsUser not found.")));
  }

  /**
   * {@code getUserPreferenceTestWithNegativeId} 
   * Equivalence Partition 4: id is negative.
   */
  @Test
  public void getUserPreferenceTestWithNegativeId() throws Exception {
    mockMvc.perform(get("/retrievePreference/-1"))
      .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User Id cannot be negative.")));
    
    mockMvc.perform(get("/retrievePreference/-2831"))
      .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User Id cannot be negative.")));
  }

  /* ======= /setPreference/{id} Equivalence Partitions ======= */

  /**
   * {@code setUserPreferenceTestValidId} 
   * Equivalence Partition 1: id is non-negative, there is a TarsUser 
   *    associated with that id, RequestBody id matches path variable id.
   */
  @Test
  public void setUserPreferenceTestValidId() throws Exception {
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("snowy");

    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("17");
    temperaturePreferences.add("15");

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
        .andExpect(jsonPath("$.temperaturePreferences", contains("17", "15")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey")));

    weatherPreferences.add("sunny");
    temperaturePreferences.add("12");
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
        .andExpect(jsonPath("$.temperaturePreferences", contains("17", "15", "12")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey", "Venice")));
  }

  /**
   * {@code setUserPreferenceMismatchId} 
   * Equivalence Partition 2: id is non-negative, there is a TarsUser 
   *    associated with that id, but RequestBody id does NOT match path variable id.
   */
  @Test
  public void setUserPreferenceMismatchId() throws Exception {
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = 
        new UserPreference(5L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/10")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Path Variable and RequestBody User Id do not match."));
  }

  /**
   * {@code setUserPreferenceWithNoTarsUser} 
   * Equivalence Partition 3: id is non-negative, but there is no TarsUser 
   *    associated with the id.
   */
  @Test
  public void setUserPreferenceWithNoTarsUser() throws Exception {
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = 
        new UserPreference(15L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/15")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));

    preference = 
        new UserPreference(0L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/0")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));
  }

  /**
   * {@code setUserPreferenceNegativeId} 
   * Equivalence Partition 4: id is negative.
   */
  @Test
  public void setUserPreferenceNegativeId() throws Exception { 
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = 
        new UserPreference(-1L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/-1")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));
    
    preference = 
        new UserPreference(-1231L, emptyPreference, emptyPreference, emptyPreference);
    mockMvc.perform(put("/setPreference/-1231")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));
  }


  /* ======= /clearPreference/{id} Equivalence Partitions ======= */

  /**
   * {@code clearPreferenceValidId} 
   *  Equivalence Partition 1: id is non-negative, there is a TarsUser 
   *    associated with that id, and userPreference for that TarsUser has been set.
   */
  @Test
  public void clearPreferenceValidId() throws Exception {
    mockMvc.perform(put("/clearPreference/1"))
        .andExpect(status().isOk())
        .andExpect(content().string("User Preference cleared successfully."));

    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isOk())
        .andExpect(content().string("User Preference cleared successfully."));
  }

  /**
   * {@code clearPreferenceNoPreferences}
   *  Equivalence Partition 2: id is non-negative, there is a TarsUser 
   *    associated with that id, but there is no existing preferences for the user.
   */
  @Test
  public void clearPreferenceNoPreferences() throws Exception {
    mockMvc.perform(put("/clearPreference/3"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User had no existing preferences."));

    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isOk())
        .andExpect(content().string("User Preference cleared successfully."));
    // Repeated clearPreference would also fall into this partition.
    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User had no existing preferences."));
  }

  /**
   * {@code clearPreferenceNoTarsUser}
   *  Equivalence Partition 3: id is non-negative but there is no TarsUser associated with the id.
   */
  @Test
  public void clearPreferenceNoTarsUser() throws Exception {
    mockMvc.perform(put("/clearPreference/0"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found.")); 
 
    mockMvc.perform(put("/clearPreference/10"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));   
  }

  /**
   * {@code clearPreferenceNegativeId} 
   * Equivalence Partition 4: id is negative.
   */
  @Test
  public void clearPreferenceNegativeId() throws Exception {
    mockMvc.perform(put("/clearPreference/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));   
    
    mockMvc.perform(put("/clearPreference/-1231"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));   
  }

  /* ======= /userPreferenceList Equivalence Partitions ======= */

  /**
   * {@code testGetUserPreferenceList}
   * Equivalence Partition 1: There is one or more TarsUsers whose preference has been set.
   */
  @Test
  public void testGetUserPreferenceList() throws Exception {
    mockMvc.perform(get("/userPreferenceList"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("sunny")))
        .andExpect(jsonPath("$[0].temperaturePreferences", contains("22")))
        .andExpect(jsonPath("$[0].cityPreferences", contains("Boston")))
        .andExpect(jsonPath("$[1].id").value(2))
        .andExpect(jsonPath("$[1].weatherPreferences", contains("rainy")))
        .andExpect(jsonPath("$[1].temperaturePreferences", contains("15", "19")))
        .andExpect(jsonPath("$[1].cityPreferences", contains("New York", "Paris")));
  }

  /**
   * {@code testGetUserPreferenceListEmpty}
   * Equivalence Partition 2: No preferences have been set for any TarsUser.
   */
  @Test
  public void testGetUserPreferenceListEmpty() throws Exception {
    // Remove the existing preferences
    mockMvc.perform(put("/clearPreference/1"))
        .andExpect(status().isOk());
    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/userPreferenceList"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  /* ======= /userPreferenceList/client/{clientId} Equivalence Partitions ======= */
  
  /**
   * {@code testGetClientUserListWithExistingPreferences}
   * Equivalence Partition 1: clientId is non-negative. Client specified by clientId exists, 
   * there exists some TarsUsers under this client that has their preferences set.
   */
  @Test
  public void testGetClientUserListWithExistingPreferences() throws Exception {
    mockMvc.perform(get("/userPreferenceList/client/1"))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].weatherPreferences", contains("sunny")))
        .andExpect(jsonPath("$[0].temperaturePreferences", contains("22")))
        .andExpect(jsonPath("$[0].cityPreferences", contains("Boston")));
  }

  /**
   * {@code testGetClientUserListWithNoPreferences}
   * Equivalence Partition 2:  clientId is non-negative. Client specified by clientId exists, 
   * there exists some TarsUsers under this client but no users that have their preferences set.
   */  
  @Test
  public void testGetClientUserListWithNoPreferences() throws Exception {
    mockMvc.perform(put("/clearPreference/2"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/userPreferenceList/client/2"))
        .andExpect(jsonPath("$[0].id").value(2))
        .andExpect(jsonPath("$[0].weatherPreferences", empty()))
        .andExpect(jsonPath("$[0].temperaturePreferences", empty()))
        .andExpect(jsonPath("$[0].cityPreferences", empty()));
  }

  /**
   * {@code testGetClientUserListWithNoUsers}
   * Equivalence Partition 3: clientId is non-negative. Client specified by clientId exists, 
   * but no TarUsers exist under this client. OR client specified by id does not exist.
   * Both return emptyList since no users are found in either case.
   */  
  @Test
  public void testGetClientUserListWithNoUsers() throws Exception {
    // Client 6 does not have users under it
    mockMvc.perform(get("/userPreferenceList/client/6"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));

    // Client 0 and Client 3030 does not exist
    mockMvc.perform(get("/userPreferenceList/client/0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
    mockMvc.perform(get("/userPreferenceList/client/3030"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  /**
   * {@code testGetClientUserListNegativeId}
   * Equivalence Partition 4: clientId is negative.
   */  
  @Test
  public void testGetClientUserListNegativeId() throws Exception {
    mockMvc.perform(get("/userPreferenceList/client/-1"))
        .andExpect(status().isBadRequest());
    
    mockMvc.perform(get("/userPreferenceList/client/-1123"))
        .andExpect(status().isBadRequest());
  }

  /* ======= Logging Branch Coverage Tests for UserPreference Endpoints ======= */

  /**
   * {@code setUserPreferenceInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void setUserPreferenceInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {
      UserPreference userPreference = new UserPreference(2L, List.of("rainy"),
          List.of("10", "15"), List.of("New York",  "Paris"));

      mockMvc.perform(put("/setPreference/2")
        .contentType("application/json")
        .content(mapper.writeValueAsString(userPreference)))
          .andExpect(status().isOk());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code clearPreferenceInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void clearPreferenceInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(put("/clearPreference/2"))
          .andExpect(status().isOk());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code retrievePreferenceInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void retrievePreferenceInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/retrievePreference/2"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(2))
          .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
          .andExpect(jsonPath("$.temperaturePreferences", contains("15", "19")))
          .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code getUserListInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getUserListInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/userPreferenceList"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(2)))
          .andExpect(jsonPath("$[0].id").value(1))
          .andExpect(jsonPath("$[1].id").value(2));


      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code getClientUserListInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getClientUserListInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/userPreferenceList/client/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].id").value(1));


      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code setUserPreferenceWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void setUserPreferenceWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      UserPreference userPreference = new UserPreference(-1L, List.of("rainy"),
          List.of("10", "15"), List.of("New York",  "Paris"));

      mockMvc.perform(put("/setPreference/-1")
        .contentType("application/json")
        .content(mapper.writeValueAsString(userPreference)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("User Id cannot be negative."));

      mockMvc.perform(put("/setPreference/2")
        .contentType("application/json")
        .content(mapper.writeValueAsString(userPreference)))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Path Variable and RequestBody User Id do not match."));    

      userPreference = new UserPreference(203L, List.of("rainy"),
        List.of("10", "15"), List.of("New York",  "Paris"));
      mockMvc.perform(put("/setPreference/203")
      .contentType("application/json")
      .content(mapper.writeValueAsString(userPreference)))
        .andExpect(status().isNotFound())
          .andExpect(content().string("TarsUser not found."));    

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code clearPreferenceWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void clearPreferenceWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {

      mockMvc.perform(put("/clearPreference/-1"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("User Id cannot be negative."));

      mockMvc.perform(put("/clearPreference/3"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("User had no existing preferences."));    

      mockMvc.perform(put("/clearPreference/6"))
          .andExpect(status().isNotFound())
          .andExpect(content().string("TarsUser not found."));  

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code retrievePreferenceWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void retrievePreferenceWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {

      mockMvc.perform(get("/retrievePreference/-1"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("User Id cannot be negative."));

      mockMvc.perform(get("/retrievePreference/3"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("User had no existing preferences."));    
      
      mockMvc.perform(get("/retrievePreference/300"))
          .andExpect(status().isNotFound())
          .andExpect(content().string("TarsUser not found."));    

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code getClientUserListWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getClientUserListWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {

      mockMvc.perform(get("/userPreferenceList/client/-1"))
          .andExpect(status().isBadRequest());  

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }
}