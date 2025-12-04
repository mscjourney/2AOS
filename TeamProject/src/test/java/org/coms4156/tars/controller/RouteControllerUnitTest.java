package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.model.TravelAdvisory;
import org.coms4156.tars.model.UserPreference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code RouteControllerUnitTest} Unit tests for RouteController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RouteControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  /* ======== GET / or GET /index Equivalence Partitions ========= */

  /**
   * {@code indexTest} Tests the index endpoint returns the welcome message.
   * Equivalence Partition 1: No input. Both / and /index always result in OK.
   */
  @Test
  public void indexTest() throws Exception {
    mockMvc.perform(get("/"))
      .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
    
    mockMvc.perform(get("/index"))
      .andExpect(status().isOk())
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
    
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/plain;charset=UTF-8"))
        .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }
  
  /* ======== /login Equivalence Partitions ========== */
  
  /**
   * {@code testLoginWithUsernameIntegration}
   * Equivalence Partition 1: RequestBody contains the username associated with an existing
   *      active TarsUser
   *    Returns the TarsUser and preference information. Preference information do not have to 
   *    already exists. Populates the ResponseEntity with Empty Preferences if not found.
   */
  @Test
  public void testLoginWithUsernameIntegration() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "alice");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").exists())
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.preferences").exists());
  }

  /**
   * {@code testLoginWithEmailIntegration}
   * Equivalence Partition 2: RequestBody contains the email associated with an existing
   *      active TarsUser
   *    Returns the TarsUser and preference information. Preference information do not have to 
   *    already exists. Populates the ResponseEntity with Empty Preferences if not found.
   */
  @Test
  public void testLoginWithEmailIntegration() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("email", "alice@gmail.com");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").exists());
  }

  /**
   * {@code testLoginWithUserIdIntegration}
   * Equivalence Partition 3: RequestBody contains the userId associated with an existing
   *      active TarsUser
   *    Returns the TarsUser and preference information. Preference information do not have to 
   *    already exists. Populates the ResponseEntity with Empty Preferences if not found.
   */
  @Test
  public void testLoginWithUserIdIntegration() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("userId", "1");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1));
  }

  /**
   * {@code testLoginWithInactiveUserIntegration}
   * Equivalence Partition 4: RequestBody has expected fields but an inactive TarsUser found.
   */
  @Test
  public void testLoginWithInactiveUserIntegration() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "bob");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isForbidden());
  }
  
  /**
   * {@code testLoginNoUserFound}
   * Equivalence Partition 5: RequestBody has expected fields but no TarsUser found.
   */
  @Test
  public void testLoginNoUserFound() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "Miranda");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isNotFound());
  }

  /**
   * {@code testLoginEmptyRequestBody}
   * Equivalence Partition 6: RequestBody is empty or populated with irrelevant fields
   */
  @Test
  public void testLoginEmptyRequestBody() throws Exception {
    Map<String, String> loginBody = new HashMap<>();

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isBadRequest());
  }

}
