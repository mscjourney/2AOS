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
@SpringBootTest(properties = {
  "security.apiKey.header=X-API-Key",
  "security.publicPaths=/,/index,/login",
  "security.adminApiKeys=adminkey000000000000000000000000"
})
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
    // Increase rate limit for the test client to avoid 429s
    long clientId = 1L;
    try {
      java.io.File f = new java.io.File("./data/clients.json");
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      com.fasterxml.jackson.core.type.TypeReference<java.util.List<
          org.coms4156.tars.model.Client>> typeRef =
          new com.fasterxml.jackson.core.type.TypeReference<
              java.util.List<org.coms4156.tars.model.Client>>() {};
      java.util.List<org.coms4156.tars.model.Client> clients =
          mapper.readValue(f, typeRef);
      if (!clients.isEmpty()) {
        clientId = clients.get(0).getClientId();
      }
    } catch (Exception ignored) { /* intentional for test setup */ }
    java.util.Map<String, Integer> payload = new java.util.HashMap<>();
    payload.put("limit", 10);
    mockMvc.perform(post("/clients/" + clientId + "/setRateLimit")
            .header("X-API-Key", "adminkey000000000000000000000000")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(payload)))
        .andExpect(status().isOk());

    TravelAdvisory mockCanadaAdvisory = 
          new TravelAdvisory("Canada", "Level 1: Exercise normal precautions", new ArrayList<>());

    mockMvc.perform(get("/country/Canada").header("X-API-Key", TestKeys.clientKey()))
      .andExpect(status().isOk())
        .andExpect(content().string(mockCanadaAdvisory.toString()));

    List<String> mockRisks = new ArrayList<>();
    mockRisks.add("Unrest (U)");
    mockRisks.add("Natural Disaster (N)");
    mockRisks.add("Terrorism (T)");

    TravelAdvisory mockIndonesiaAdvisory =
          new TravelAdvisory("Indonesia", "Level 2: Exercise increased caution", mockRisks);
    
    mockMvc.perform(get("/country/Indonesia").header("X-API-Key", TestKeys.clientKey()))
        .andExpect(status().isOk())
        .andExpect(content().string(mockIndonesiaAdvisory.toString()));

    mockMvc.perform(get("/country/Earth").header("X-API-Key", TestKeys.clientKey()))
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

// Helper moved to its own file to satisfy OneTopLevelClass rule.
