package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import org.coms4156.tars.util.LoggerTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code CountryEndpointTest}
 * Contains Equivalence Partitions testing for Country Endpoints.
 * Covers:
 *    GET /country/{country}
 *    GET /countrySummary/{country}
 */
@SpringBootTest
@AutoConfigureMockMvc
public class CountryEndpointTest {
  /**
   * Disable API key security for tests to prevent 401 responses.
   */
  @org.junit.jupiter.api.BeforeAll
  public static void disableSecurity() {
    System.setProperty("security.enabled", "false");
  }

  @Autowired
  private MockMvc mockMvc;

  /* ======== /country/{country} ======== */

  /**
   * {@code testGetCountryAdvisoryValidCountry}
   * Equivalence Partition 1: country is an valid existing country.
   * Case Sensitivity of the country does not matter.
   */
  @Test
  void testGetCountryAdvisoryValidCountry() throws Exception {
    mockMvc.perform(get("/country/United States")
        .header("X-API-Key", TestKeys.clientKey()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = United States")))
        .andExpect(content().string(containsString("level = No advisory")))
        .andExpect(content().string(
          containsString("riskIndicators = [No nationwide travel advisory issued]")));
    // case insensitive
    mockMvc.perform(get("/country/uniTeD statEs")
        .header("X-API-Key", TestKeys.clientKey()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("country = United States")))
        .andExpect(content().string(containsString("level = No advisory")))
        .andExpect(content().string(
          containsString("riskIndicators = [No nationwide travel advisory issued]")));
    // case insensitive
    mockMvc.perform(get("/country/united states")
        .header("X-API-Key", TestKeys.clientKey()))
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
   * {@code testGetCountryAdvisoryInvalidCountry}
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

  /* ======== /countrySummary/{country} Equivalence Partitions */

  /**
   * {@code testGetCountrySummaryValidCountry}
   * Equivalence Partition 1: country is an valid existing country.
   * Case Sensitivity of the country does not matter.
   */
  @Test
  void testGetCountrySummaryValidCountry() throws Exception {
    mockMvc.perform(get("/countrySummary/United States"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("country").value("United States"))
        .andExpect(jsonPath("message", containsString("diverse landscapes")))
        .andExpect(jsonPath("message", containsString("entertainment attractions")));

    mockMvc.perform(get("/countrySummary/uniTeD sTates"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("country").value("United States"))
        .andExpect(jsonPath("message", containsString("diverse landscapes")))
        .andExpect(jsonPath("message", containsString("entertainment attractions")));
    
    mockMvc.perform(get("/countrySummary/Albania"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("country").value("Albania"))
        .andExpect(jsonPath("message", containsString("Adriatic beaches")))
        .andExpect(jsonPath("message", containsString("Mediterranean-style")));
  }
  
  /** 
   * {@code testGetCountrySummaryInvalidCountry}
   * Equivalence Partition 2: country is invalid and does not exist in the world.
   */
  @Test
  void testGetCountrySummaryInvalidCountry() throws Exception {
    // Valid Cities are not countries
    mockMvc.perform(get("/countrySummary/New York"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Country not found: New York"));
    
    mockMvc.perform(get("/countrySummary/Jupiter"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Country not found: Jupiter"));
    
    mockMvc.perform(get("/countrySummary/123!"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Country not found: 123!"));
  }

  /* ======= Logging Branch Coverage for Country Endpoints ======== */

  /**
   * {@code getCountrySummaryInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getCountrySummaryInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/countrySummary/Japan"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("country").value("Japan"))
          .andExpect(jsonPath("message", containsString("traditional temples")));

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code getCountrySummaryWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getCountrySummaryWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      
      mockMvc.perform(get("/countrySummary/Galaxy"))
          .andExpect(status().isNotFound())
          .andExpect(content().string("Country not found: Galaxy"));

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }
}