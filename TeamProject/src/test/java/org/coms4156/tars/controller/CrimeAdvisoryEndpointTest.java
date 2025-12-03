package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.util.LoggerTestUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code CrimeAdvisoryEndpointTest}
 * Contains Equivalence Partitions testing for Crime and TravelAdvisory Endpoints.
 * Covers:
 *    - GET /crime/summary?state={state}&offense={offense}&month={month}&year={year}
 *    - GET /country/{country}
 */
@SpringBootTest
@AutoConfigureMockMvc
public class CrimeAdvisoryEndpointTest {

  @Autowired
  private MockMvc mockMvc;

  /* ======== /crime/summary Equivalence Partitions ======== */

  /**
   * {@code testGetCrimeSummaryWithValidParams}
   * Equivalence Partition 1: All Valid Inputs.
   * state : the full name or the abbreviation of a valid US state. Not case sensitive.
   * offense : a valid crime offense code
   * month: two-digit string representing the month MM
   * year : a four-digit string representing the year YYYY.
   * Will return the crime data on the offense code for the month specified by MM/YYYY 
   *    in specified state 
   */
  @Test
  void testGetCrimeSummaryWithValidParams() throws Exception {
    mockMvc.perform(get("/crime/summary")
                .param("state", "NC")
                .param("offense", "V")
                .param("month", "10")
                .param("year", "2025"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("NC"))
        .andExpect(jsonPath("$.month").value("10"))
        .andExpect(jsonPath("$.year").value("2025"))
        .andExpect(jsonPath("$.message")
                .value(containsString("cases per 100,000 people")));
      
    mockMvc.perform(get("/crime/summary")
                  .param("state", "ny")// works with lowercase
                  .param("offense", "V")
                  .param("month", "10")
                  .param("year", "2025"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.state").value("ny"))
        .andExpect(jsonPath("$.month").value("10"))
        .andExpect(jsonPath("$.year").value("2025"));
  }

  /**
   * {@code testGetCrimeSummaryInvalidParams}
   * Equivalence Partition 2: Any Invalid Input.
   * We do not distinguish between the types of invalid inputs as the API will return BAD_REQUEST
   * if any of the four parameters are malformed.
   * Examples of Invalid Inputs 
   *    state is not a valid US state name or any of its abbreviation (including DC)
   *    offense code specified does not correspond to a real crime category
   *    month is not in range 01 - 12
   *    year is in the future or too far in the past.
   */
  @Test
  void testGetCrimeSummaryInvalidParams() throws Exception {
    // Year is in the future
    mockMvc.perform(get("/crime/summary")
                    .param("state", "NJ")
                    .param("offense", "HOM")
                    .param("month", "05")
                    .param("year", "2030"))
              .andExpect(status().isBadRequest())
              .andExpect(content().string("Error: Invalid inputs have been passed in."));

    // Month is not valid
    mockMvc.perform(get("/crime/summary")
                    .param("state", "NJ")
                    .param("offense", "HOM")
                    .param("month", "15")
                    .param("year", "2030"))
              .andExpect(status().isBadRequest())
              .andExpect(content().string("Error: Invalid inputs have been passed in."));

    // Invalid Offense type
    mockMvc.perform(get("/crime/summary")
                .param("state", "NJ")
                .param("offense", "United")
                .param("month", "05")
                .param("year", "2022"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Error: Invalid inputs have been passed in."));

    // Invalid State Abbreviation
    mockMvc.perform(get("/crime/summary")
                .param("state", "XP")
                .param("offense", "ASS")
                .param("month", "05")
                .param("year", "2025"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Error: Invalid inputs have been passed in."));
    
    // Invalid State Name
    mockMvc.perform(get("/crime/summary")
                .param("state", "Berlin")
                .param("offense", "V")
                .param("month", "03")
                .param("year", "2020"))
          .andExpect(status().isBadRequest())
          .andExpect(content().string("Error: Invalid inputs have been passed in."));
    
    // Not all params have been passed in
    mockMvc.perform(get("/crime/summary")
                .param("state", "NC"))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code testGetCrimeSummaryHandlesException}
   * Test when the API throws exception.
   */
  @Test
  void testGetCrimeSummaryHandlesException() throws Exception {
    try (MockedConstruction<CrimeModel> mocked = Mockito.mockConstruction(
            CrimeModel.class,
            (mock, context) -> when(mock.getCrimeSummary("NC", "V", "10", "2025"))
                    .thenThrow(new RuntimeException()))) {

      mockMvc.perform(get("/crime/summary")
                      .param("state", "NC")
                      .param("offense", "V")
                      .param("month", "10")
                      .param("year", "2025"))
              .andExpect(status().isInternalServerError());
    }
  }

  /* ======== /country/{country} ======== */

  /**
   * {@code testGetCountryAdvisoryValidCountry}
   * Equivalence Partition 1: country is an valid existing country.
   * Case Sensitivity of the country does not matter.
   */
  @Test
  void testGetCountryAdvisoryValidCountry() throws Exception {
    mockMvc.perform(get("/country/United States"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = United States")))
        .andExpect(content().string(containsString("level = No advisory")))
        .andExpect(content().string(
          containsString("riskIndicators = [No nationwide travel advisory issued]")));
    // case insensitive
    mockMvc.perform(get("/country/uniTeD statEs"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = United States")))
        .andExpect(content().string(containsString("level = No advisory")))
        .andExpect(content().string(
          containsString("riskIndicators = [No nationwide travel advisory issued]")));
    // case insensitive
    mockMvc.perform(get("/country/united states"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = United States")))
        .andExpect(content().string(containsString("level = No advisory")))
        .andExpect(content().string(
          containsString("riskIndicators = [No nationwide travel advisory issued]")));
    
    mockMvc.perform(get("/country/Germany"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = Germany")))
        .andExpect(content().string(containsString("level = Level 2: Exercise increased caution")));
  }

  /** 
   * {@code }
   * Equivalence Partition 2: country is invalid and does not exist in the world.
   */
  @Test
  void testGetCountryAdvisoryInvalidCountry() throws Exception {
    // Cities are not countries
    mockMvc.perform(get("/country/Berlin"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("No such country found."));
    
    mockMvc.perform(get("/country/Earth"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("No such country found."));
    
    mockMvc.perform(get("/country/!s(A)"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("No such country found."));
  }

  /* ======= Logging Branch Coverage Tests for Crime and Advisory Endpoints ======= */

  /**
   * {@code getCrimeSummaryInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getCrimeSummaryInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/crime/summary")
                  .param("state", "NC")
                  .param("offense", "V")
                  .param("month", "10")
                  .param("year", "2025"))
            .andExpect(status().isOk());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  
  /**
   * {@code getCrimeSummaryWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getCrimeSummaryWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      
      mockMvc.perform(get("/crime/summary")
                  .param("state", "Berlin")
                  .param("offense", "V")
                  .param("month", "03")
                  .param("year", "2020"))
            .andExpect(status().isBadRequest());

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }
}