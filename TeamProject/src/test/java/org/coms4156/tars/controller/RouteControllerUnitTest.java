package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.coms4156.tars.model.TravelAdvisory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code RouteControllerUnitTest} Unit tests for RouteController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class RouteControllerUnitTest {

  @Autowired
  private MockMvc mockMvc;

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
}
