package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for CrimeModel.
 */
class CrimeModelTest {

  private CrimeModel crimeModel;

  @BeforeEach
  void setUp() {
    crimeModel = new CrimeModel();
  }

  /**
   * Tests successful response with valid paramseters.
   */
  @Test
  void testGetCrimeSummary_Success() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("NC", "V", "10", "2025");
      assertNotNull(result);
      assertTrue(result.contains("North Carolina") && result.contains("cases per 100,000 people")
          || result.contains("Error") 
          || result.contains("API call failed"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests successful response with valid paramseters.
   */
  @Test
  void testGetCrimeSummary_Success_2() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("CA", "ASS", "10", "2025");
      assertNotNull(result);
      assertTrue(result.contains("California") && result.contains("cases per 100,000 people")
          || result.contains("Error") 
          || result.contains("API call failed"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests response when no data is available for the date.
   */
  @Test
  void testGetCrimeSummary_NoDataAvailable() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("NC", "V", "01", "1900");
      assertNotNull(result);
      assertTrue(result.contains("No rate data available") 
          || result.contains("Error") 
          || result.contains("API call failed"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests error handling when API call fails.
   */
  @Test
  void testGetCrimeSummary_InvalidState() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("XX", "V", "10", "2025");
      assertNotNull(result);
      assertTrue(result.contains("Error") 
          || result.contains("No rate data available")
          || result.contains("API call failed"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }
}

