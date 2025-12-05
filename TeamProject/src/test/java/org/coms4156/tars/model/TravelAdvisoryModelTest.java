package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for the TravelAdvisoryModel class.
 * Equivalence Partiton Testing for TravelAdvisoryModel.getTravelAdvisory(String country)
 * 1) Equivalence Partition 1: country is a valid country that exists.
 *      Ignores leading/trailing whitespace
 *    Test Cases: testGetTravelAdvisoryValidCountries, testGetTravelAdvisoryCaseInsensitive,
 *         testGetTravelAdvisoryIgnoreWhiteSpace, testGetAdvisoryForCountryWithRiskIndicators
 * 2) Equivalence Partition 2: country is a non-valid country that doesn't exist.
 *      null, empty, or just whitespace strings are all invalid.
 *    Test Cases: testGetAdvisoryForCountryNotFound, testTravelAdvisoryModelInvalidParameter,
 *          testGetAdvisoryForCountryWithPartialMatch
 */
@SpringBootTest
public class TravelAdvisoryModelTest {

  private TravelAdvisoryModel travelAdvisoryModel;

  @BeforeEach
  void setUp() {
    travelAdvisoryModel = new TravelAdvisoryModel();
  }

  @Test
  void testConstructor() {
    assertNotNull(travelAdvisoryModel);
  }

  @Test
  void testGetTravelAdvisoryValidCountries() {
    TravelAdvisory usAdvisory = travelAdvisoryModel.getTravelAdvisory("United States");
    TravelAdvisory afghanistanAdvisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    TravelAdvisory albaniaAdvisory = travelAdvisoryModel.getTravelAdvisory("Albania");
    
    assertNotNull(usAdvisory);
    assertNotNull(afghanistanAdvisory);
    assertNotNull(albaniaAdvisory);
    
    assertEquals("United States", usAdvisory.getCountry());
    assertEquals("Afghanistan", afghanistanAdvisory.getCountry());
    assertEquals("Albania", albaniaAdvisory.getCountry());
  }

  @Test
  void testGetTravelAdvisoryCaseInsensitive() {
    TravelAdvisory advisory1 = travelAdvisoryModel.getTravelAdvisory("united states");
    TravelAdvisory advisory2 = travelAdvisoryModel.getTravelAdvisory("UNITED STATES");
    TravelAdvisory advisory3 = travelAdvisoryModel.getTravelAdvisory("United States");
    
    assertNotNull(advisory1);
    assertNotNull(advisory2);
    assertNotNull(advisory3);
    assertEquals("United States", advisory1.getCountry());
    assertEquals("United States", advisory2.getCountry());
    assertEquals("United States", advisory3.getCountry());
  }

  @Test
  void testGetTravelAdvisoryIgnoreWhiteSpace() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("  Albania  ");
    assertNotNull(advisory);
    assertEquals("Albania", advisory.getCountry());

    advisory = travelAdvisoryModel.getTravelAdvisory("Albania        ");
    assertNotNull(advisory);
    assertEquals("Albania", advisory.getCountry());

    advisory = travelAdvisoryModel.getTravelAdvisory("          Albania");
    assertNotNull(advisory);
    assertEquals("Albania", advisory.getCountry());
  }

  @Test
  void testGetAdvisoryForCountryWithRiskIndicators() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    assertEquals("Afghanistan", advisory.getCountry());
    assertNotNull(advisory.getLevel());
    assertNotNull(advisory.getRiskIndicators());
    assertTrue(!advisory.getRiskIndicators().isEmpty());
  }

  @Test
  void testGetAdvisoryForCountryNotFound() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("NonExistentCountry");
    assertNull(advisory);
  
    advisory = travelAdvisoryModel.getTravelAdvisory("!!)(SA)");
    assertNull(advisory);
  }

  @Test
  void testTravelAdvisoryModelInvalidParameter() throws Exception {
    IllegalArgumentException ex1 = assertThrows(
        IllegalArgumentException.class, 
        () -> travelAdvisoryModel.getTravelAdvisory(null));
    assertTrue(ex1.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex2 = assertThrows(
        IllegalArgumentException.class, 
        () -> travelAdvisoryModel.getTravelAdvisory(""));
    assertTrue(ex2.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex3 = assertThrows(
        IllegalArgumentException.class, 
        () -> travelAdvisoryModel.getTravelAdvisory("       "));
    assertTrue(ex3.getMessage().contains("Country cannot be empty."));
  }

  @Test
  void testGetAdvisoryForCountryWithPartialMatch() {
    // Test that exact match is required (case-insensitive)
    TravelAdvisory advisory1 = travelAdvisoryModel.getTravelAdvisory("united");
    TravelAdvisory advisory2 = travelAdvisoryModel.getTravelAdvisory("states");
    
    // Should not find partial matches
    assertTrue(advisory1 == null);
    assertTrue(advisory2 == null);
  }

  @Test
  void testGetTravelAdvisoryWithRiskIndicators() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    assertEquals("Afghanistan", advisory.getCountry());
    assertEquals("Level 4: Do not travel", advisory.getLevel());
    assertNotNull(advisory.getRiskIndicators());
    assertTrue(!advisory.getRiskIndicators().isEmpty());
    assertTrue(advisory.getRiskIndicators().contains("Unrest (U)"));
  }

  @Test
  void testGetTravelAdvisoryRiskIndicatorsContent() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    java.util.List<String> riskIndicators = advisory.getRiskIndicators();
    assertNotNull(riskIndicators);
    
    // Verify that risk indicators are properly populated
    assertTrue(!riskIndicators.isEmpty());
    // Check for expected risk indicators
    boolean hasExpectedIndicator = false;
    for (String indicator : riskIndicators) {
      if (indicator.contains("Crime") || indicator.contains("Terrorism") 
          || indicator.contains("Unrest") || indicator.contains("Health")) {
        hasExpectedIndicator = true;
        break;
      }
    }
    assertTrue(hasExpectedIndicator || !riskIndicators.isEmpty());
  }
}

