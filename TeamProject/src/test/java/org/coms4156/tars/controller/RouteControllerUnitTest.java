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
  public void testGetCitySummaryWithNullCity() throws Exception {
    mockMvc.perform(get("/summary/  ")) // empty path variable
        .andExpect(status().isBadRequest())
          .andExpect(content().string("City cannot be empty."));
  }

  @Test
  public void testGetCitySummaryWithCityOnly() throws Exception {
    mockMvc.perform(get("/summary/New York"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").exists());
  }

  @Test
  public void testGetCitySummaryWithInvalidStartDate() throws Exception {
    mockMvc.perform(get("/summary/New York")
            .param("startDate", "invalid-date"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Invalid startDate format")));
  }

  @Test
  public void testGetCitySummaryWithInvalidEndDate() throws Exception {
    mockMvc.perform(get("/summary/New York")
            .param("endDate", "invalid-date"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Invalid endDate format")));
  }

  @Test
  public void testGetCitySummaryWithStartDateAfterEndDate() throws Exception {
    mockMvc.perform(get("/summary/New York")
            .param("startDate", "2024-01-15")
            .param("endDate", "2024-01-01"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("startDate cannot be after endDate")));
  }

  @Test
  public void testGetCitySummaryWithValidParameters() throws Exception {
    mockMvc.perform(get("/summary/Boston")
            .param("startDate", "2024-06-01")
            .param("endDate", "2024-06-14"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").exists());
  }

  @Test
  public void testGetCitySummaryWithLongerThanFourteenDays() throws Exception {
    mockMvc.perform(get("/summary/San Francisco")
            .param("startDate", "2025-08-01")
            .param("endDate", "2025-10-01"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").exists());
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
