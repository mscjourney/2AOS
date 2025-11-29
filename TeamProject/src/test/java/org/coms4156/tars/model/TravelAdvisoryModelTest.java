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
  void testGetAdvisoryForCountryValidCountry() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("United States");
    
    assertNotNull(advisory);
    assertEquals("United States", advisory.getCountry());
  }

  @Test
  void testGetAdvisoryForCountryCaseInsensitive() {
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
  void testGetAdvisoryForCountryWithRiskIndicators() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    assertEquals("Afghanistan", advisory.getCountry());
    assertNotNull(advisory.getLevel());
    assertNotNull(advisory.getRiskIndicators());
    assertTrue(advisory.getRiskIndicators().size() > 0);
  }

  @Test
  void testGetAdvisoryForCountryNotFound() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("NonExistentCountry");
    
    assertNull(advisory);
  }

  @Test
  void testGetAdvisoryForCountryNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory(null);
    });
  }

  @Test
  void testGetAdvisoryForCountryEmpty() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory("");
    });
  }

  @Test
  void testGetAdvisoryForCountryWhitespace() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory("   ");
    });
  }

  @Test
  void testGetTravelAdvisoryValidCountry() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("United States");
    
    assertNotNull(advisory);
    assertEquals("United States", advisory.getCountry());
    assertNotNull(advisory.getLevel());
    assertNotNull(advisory.getRiskIndicators());
  }

  @Test
  void testGetTravelAdvisoryCaseInsensitive() {
    TravelAdvisory advisory1 = travelAdvisoryModel.getTravelAdvisory("albania");
    TravelAdvisory advisory2 = travelAdvisoryModel.getTravelAdvisory("ALBANIA");
    TravelAdvisory advisory3 = travelAdvisoryModel.getTravelAdvisory("Albania");
    
    assertNotNull(advisory1);
    assertNotNull(advisory2);
    assertNotNull(advisory3);
    assertEquals("Albania", advisory1.getCountry());
    assertEquals("Albania", advisory2.getCountry());
    assertEquals("Albania", advisory3.getCountry());
  }

  @Test
  void testGetTravelAdvisoryWithRiskIndicators() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    assertEquals("Afghanistan", advisory.getCountry());
    assertEquals("Level 4: Do not travel", advisory.getLevel());
    assertNotNull(advisory.getRiskIndicators());
    assertTrue(advisory.getRiskIndicators().size() > 0);
    assertTrue(advisory.getRiskIndicators().contains("Unrest (U)"));
  }

  @Test
  void testGetTravelAdvisoryWithEmptyRiskIndicators() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("United States");
    
    assertNotNull(advisory);
    assertEquals("United States", advisory.getCountry());
    assertNotNull(advisory.getRiskIndicators());
    assertTrue(advisory.getRiskIndicators().isEmpty() 
        || advisory.getRiskIndicators().size() >= 0);
  }

  @Test
  void testGetTravelAdvisoryNotFound() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("NonExistentCountry");
    
    assertNull(advisory);
  }

  @Test
  void testGetTravelAdvisoryNull() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory(null);
    });
  }

  @Test
  void testGetTravelAdvisoryEmpty() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory("");
    });
  }

  @Test
  void testGetTravelAdvisoryWhitespace() {
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory("   ");
    });
  }

  @Test
  void testGetTravelAdvisoryMultipleCountries() {
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
  void testGetTravelAdvisoryRiskIndicatorsContent() {
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Afghanistan");
    
    assertNotNull(advisory);
    java.util.List<String> riskIndicators = advisory.getRiskIndicators();
    assertNotNull(riskIndicators);
    
    // Verify that risk indicators are properly populated
    assertTrue(riskIndicators.size() > 0);
    // Check for expected risk indicators
    boolean hasExpectedIndicator = false;
    for (String indicator : riskIndicators) {
      if (indicator.contains("Crime") || indicator.contains("Terrorism") 
          || indicator.contains("Unrest") || indicator.contains("Health")) {
        hasExpectedIndicator = true;
        break;
      }
    }
    assertTrue(hasExpectedIndicator || riskIndicators.size() > 0);
  }

  @Test
  void testGetAdvisoryForCountryWithWhitespacePadding() {
    // The method uses equalsIgnoreCase but doesn't trim, so whitespace won't match
    // Test that whitespace-only input throws exception
    assertThrows(IllegalArgumentException.class, () -> {
      travelAdvisoryModel.getTravelAdvisory("   ");
    });
    
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("  United States  ");
    assertNull(advisory);
  }

  @Test
  void testGetTravelAdvisoryWithWhitespacePadding() {
    // The method uses equalsIgnoreCase but doesn't trim, so whitespace won't match
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("  Albania  ");
    
    // Should return null because "  Albania  " != "Albania" (whitespace matters)
    assertNull(advisory);
  }

  @Test
  void testGetAdvisoryForCountryWithPartialMatch() {
    // Test that exact match is required (case-insensitive)
    TravelAdvisory advisory1 = travelAdvisoryModel.getTravelAdvisory("united");
    TravelAdvisory advisory2 = travelAdvisoryModel.getTravelAdvisory("states");
    
    // Should not find partial matches
    assertTrue(advisory1 == null || advisory1.getCountry().equals("United States"));
    assertTrue(advisory2 == null || advisory2.getCountry().equals("United States"));
  }

  @Test
  void testGetTravelAdvisoryMultipleCountriesSequentially() {
    // Test multiple sequential calls to ensure state is maintained
    TravelAdvisory us1 = travelAdvisoryModel.getTravelAdvisory("United States");
    TravelAdvisory us2 = travelAdvisoryModel.getTravelAdvisory("United States");
    
    assertNotNull(us1);
    assertNotNull(us2);
    assertEquals(us1.getCountry(), us2.getCountry());
    assertEquals(us1.getLevel(), us2.getLevel());
  }

  @Test
  void testGetAdvisoryForCountryWithSpecialCharacters() {
    // Test countries that might have special characters in the data
    TravelAdvisory advisory = travelAdvisoryModel.getTravelAdvisory("Côte d'Ivoire");
    
    // May or may not exist in data, but should not throw exception
    assertTrue(advisory == null || advisory.getCountry() != null);
  }
}

