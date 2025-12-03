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

  /* ======== GET / or GET /index Equivalence Partition ========= */

  /**
   * {@code indexTest} Tests the index endpoint returns the welcome message.
   * Equivalence Partition 1: No input. Both / and /index always result in OK.
   *
   * @throws Exception if the request fails
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
  
  /* Other Tests */

  @Test
  public void testGetCountryAdvisory() throws Exception {
    TravelAdvisory mockCanadaAdvisory = 
          new TravelAdvisory("Canada", "Level 1: Exercise normal precautions", new ArrayList<>());

    mockMvc.perform(get("/country/Canada"))
      .andExpect(status().isOk())
        .andExpect(content().string(mockCanadaAdvisory.toString()));

    List<String> mockRisks = new ArrayList<>();
    mockRisks.add("Unrest (U)");
    mockRisks.add("Natural Disaster (N)");
    mockRisks.add("Terrorism (T)");

    TravelAdvisory mockIndonesiaAdvisory =
          new TravelAdvisory("Indonesia", "Level 2: Exercise increased caution", mockRisks);
    
    mockMvc.perform(get("/country/Indonesia"))
      .andExpect(status().isOk())
        .andExpect(content().string(mockIndonesiaAdvisory.toString()));

    mockMvc.perform(get("/country/Earth"))
        .andExpect(status().isNotFound());
  }

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

  @Test
  public void testLoginWithInactiveUserIntegration() throws Exception {
    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "bob");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isForbidden());
  }
}
