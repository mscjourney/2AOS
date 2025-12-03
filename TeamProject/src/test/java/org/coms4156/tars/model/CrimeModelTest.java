package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
   * Tests successful response with valid parameters.
   */
  @Test
  void testGetCrimeSummary_Success() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("NC", "V", "12", "2025");
      assertNotNull(result);
      assertTrue(result.contains("North Carolina"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests successful response with valid parameters.
   */
  @Test
  void testGetCrimeSummary_Success_2() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("CA", "ASS", "12", "2025");
      assertNotNull(result);
      assertTrue(result.contains("California"));

      result = model.getCrimeSummary("ca", "ASS", "12", "2025");
      assertNotNull(result);
      assertTrue(result.contains("California"));
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests response when unexpected date is inputted.
   */
  @Test
  void testGetCrimeSummary_InvalidDate() {
    CrimeModel model = new CrimeModel();
    try {
      String result = model.getCrimeSummary("NC", "V", "01", "1900");
      assertNull(result);

      result = model.getCrimeSummary("NY", "HOM", "06", "2040");
      assertNull(result);
    } catch (RuntimeException e) {
      assertTrue(false);
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests response when unexpected state is inputted.
   */
  @Test
  void testGetCrimeSummary_InvalidState() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("XX", "V", "10", "2025");
      assertNull(result);
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  /**
   * Tests response when unexpected offense is inputted.
   */
  @Test
  void testGetCrimeSummary_InvalidOffense() {
    CrimeModel model = new CrimeModel();

    try {
      String result = model.getCrimeSummary("CA", "COMS", "10", "2025");
      assertNull(result);
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  @Test
  void testGetCrimeSummary_FullStateName() {
    CrimeModel model = new CrimeModel();
    try {
      String result = model.getCrimeSummary("Maine", "ASS", "10", "2025");
      assertNotNull(result);
      assertTrue(result.contains("Maine"));

      result = model.getCrimeSummary("District of Columbia", "V", "06", "2025");
      assertNotNull(result);
      assertTrue(result.contains("District of Columbia"));
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }

  @Test
  void testGetCrimeSummary_InvalidStateName() {
    CrimeModel model = new CrimeModel();
    try {
      String result = model.getCrimeSummary("Mars", "ASS", "08", "2025");
      assertNull(result);
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("API call failed"));
    }
  }
}



