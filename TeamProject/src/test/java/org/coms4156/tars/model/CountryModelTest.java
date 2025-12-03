package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for the Client model class.
 */
@SpringBootTest
public class CountryModelTest {

  private final CountryModel countryModel = new CountryModel();

  @Test
  void testCountrySummaryInvalidParameter() throws Exception {
    IllegalArgumentException ex1 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountrySummary(null));
    assertTrue(ex1.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex2 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountrySummary(""));
    assertTrue(ex2.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex3 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountrySummary("       "));
    assertTrue(ex3.getMessage().contains("Country cannot be empty."));
  }

  @Test
  void testCountrySummaryValidCountry() throws Exception {
    CountrySummary russiaSummary = countryModel.getCountrySummary("Russia");
    assertNotNull(russiaSummary);
    assertEquals(russiaSummary.getCountry(), "Russia");
    assertEquals(russiaSummary.getCapital(), "Moscow");
    assertTrue(russiaSummary.getMessage().contains("vast cultural heritage and historic cities"));

    CountrySummary lebanonSummary = countryModel.getCountrySummary("Lebanon");
    assertNotNull(lebanonSummary);
    assertEquals(lebanonSummary.getCountry(), "Lebanon");
    assertEquals(lebanonSummary.getCapital(), "Beirut");
    assertTrue(lebanonSummary.getMessage().contains("ancient ruins, Mediterranean cuisine,"));
  }

  @Test
  void testCountrySummaryInvalidCountry() {
    CountrySummary earthSummary = countryModel.getCountrySummary("Earth");
    assertNull(earthSummary);

    CountrySummary londonSummary = countryModel.getCountrySummary("London");
    assertNull(londonSummary);
  }
}