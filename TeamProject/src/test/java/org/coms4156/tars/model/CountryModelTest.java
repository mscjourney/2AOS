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

  private CountryModel countryModel = new CountryModel();

  @Test
  void testGetCountryInfoValidCountry() {
    JsonNode usNode = countryModel.getCountryInfo("United States");
    assertNotNull(usNode);
    assertEquals("United States", usNode.get("country").asText());
    assertEquals("Washington, DC", usNode.get("capital").asText());
    assertTrue(usNode.get("summary").asText().contains("diverse landscapes"));
    assertTrue(usNode.get("summary").asText().contains("and entertainment attractions."));
    assertTrue(usNode.get("summary").asText().contains("national parks and coastlines"));

    JsonNode norwayNode = countryModel.getCountryInfo("Norway");
    assertNotNull(norwayNode);
    assertEquals("Norway", norwayNode.get("country").asText());
    assertEquals("Oslo", norwayNode.get("capital").asText());
    assertTrue(norwayNode.get("summary").asText().contains("fjords, Northern Lights"));
    assertTrue(norwayNode.get("summary").asText().contains("pristine outdoor adventure"));
  }

  @Test
  void testGetCountryInfoInvalidCountry() {
    JsonNode node = countryModel.getCountryInfo("Mars");
    assertNull(node);

    node = countryModel.getCountryInfo("New York");
    assertNull(node);
  }

  @Test
  void testGetCountryInfoInvalidParameter() throws Exception {
    IllegalArgumentException ex1 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountryInfo(null));
    assertTrue(ex1.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex2 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountryInfo(""));
    assertTrue(ex2.getMessage().contains("Country cannot be empty."));

    IllegalArgumentException ex3 = assertThrows(
        IllegalArgumentException.class, 
        () -> countryModel.getCountryInfo("       "));
    assertTrue(ex3.getMessage().contains("Country cannot be empty."));
  }

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