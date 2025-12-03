package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * {@code WeatherModelTest} Unit tests for WeatherModel class.
 * 
 * <p>Equivalence Partitions for WeatherModel Methods
 * ======== {@code String getWeatherForCity(String city, int days)} =============
 * ======== {@code WeatherRecommendation getRecommendedDays(String city, int days)} ========
 * == {@code WeatherRecommendation getUserRecDays(String city, int days, UserPreference user)} ==
 * 
 * 
 */
public class WeatherModelTest {

  @Test
  public void testGetRecommendedDaysWithValidCity() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("New York", 7);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("New York", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    assertFalse(recommendation.getMessage().contains("Error"), 
        "Should not contain error message for valid city");
  }

  @Test
  public void testGetRecommendedDaysWithValidCityAndDays() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Boston", 5);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
  }

  @Test
  public void testGetRecommendedDaysWithCityContainingSpaces() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("San Francisco", 3);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("San Francisco", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
  }

  @Test
  public void testGetRecommendedDaysWithInvalidCity() {
    String invalidCity = "NonExistentCity12345XYZ";
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(invalidCity, 7);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals(invalidCity, recommendation.getCity(), 
        "City should match input even if invalid");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertTrue(recommendation.getMessage().contains("Error") 
        || recommendation.getMessage().contains("Could not find coordinates")
        || recommendation.getMessage().contains("No clear days"),
        "Should indicate error or no clear days for invalid city");
  }

  @Test
  public void testGetRecommendedDaysWithEmptyCityName() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("", 7);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("", recommendation.getCity(), 
        "City should be empty string");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
  }

  @Test
  public void testGetRecommendedDaysWithSingleDay() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("London", 1);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("London", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    assertTrue(recommendation.getRecommendedDays().size() <= 1, 
        "Should have at most 1 recommended day for 1 day forecast");
  }

  @Test
  public void testGetRecommendedDaysWithMaximumDays() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Paris", 14);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Paris", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    assertTrue(recommendation.getRecommendedDays().size() <= 14, 
        "Should have at most 14 recommended days for 14 day forecast");
  }

  @Test
  public void testGetRecommendedDaysMessageWhenNoClearDays() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Tokyo", 7);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    
    boolean hasClearDaysMessage = recommendation.getMessage()
        .contains("These days are expected to have clear weather");
    boolean hasNoClearDaysMessage = recommendation.getMessage()
        .contains("No clear days");
    boolean hasErrorMessage = recommendation.getMessage().contains("Error");
    
    assertTrue(hasClearDaysMessage || hasNoClearDaysMessage || hasErrorMessage, 
        "Message should indicate clear days, no clear days, or error");
  }

  @Test
  public void testGetRecommendedDaysMessageWhenHasClearDays() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Chicago", 7);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    
    if (!recommendation.getRecommendedDays().isEmpty()) {
      assertTrue(recommendation.getMessage()
          .contains("These days are expected to have clear weather"),
          "Message should indicate clear weather when there are recommended days");
    }
  }

  @Test
  public void testGetRecommendedDaysWithSpecialCharactersInCity() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("São Paulo", 5);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("São Paulo", recommendation.getCity(), 
        "City should match input with special characters");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
  }

  @Test
  public void testGetRecommendedDaysReturnsWeatherRecommendationObject() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Miami", 3);

    assertNotNull(recommendation, "Should return a WeatherRecommendation object");
    assertTrue(recommendation instanceof WeatherRecommendation, 
        "Should return instance of WeatherRecommendation");
  }

  @Test
  public void testGetRecommendedDaysWithDifferentDayCounts() {
    // Test that the method works with different day counts
    for (int days = 1; days <= 7; days++) {
      WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("Seattle", days);

      assertNotNull(recommendation, 
          "Recommendation should not be null for " + days + " days");
      assertEquals("Seattle", recommendation.getCity(), 
          "City should match for " + days + " days");
      assertNotNull(recommendation.getRecommendedDays(), 
          "Recommended days list should not be null for " + days + " days");
      assertTrue(recommendation.getRecommendedDays().size() <= days, 
          "Should have at most " + days + " recommended days");
    }
  }

  @Test
  public void testGetRecommendedDaysHandlesNetworkErrorsGracefully() {
    String longCityName = "A".repeat(1000);
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(longCityName, 7);

    assertNotNull(recommendation, 
        "Should return a recommendation even on error");
    assertEquals(longCityName, recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getMessage(), 
        "Message should not be null");
    assertTrue(recommendation.getMessage().contains("Error") 
        || recommendation.getMessage().contains("Could not find coordinates")
        || recommendation.getRecommendedDays().isEmpty(),
        "Should indicate error or empty result");
  }

  @Test
  public void testGetRecommendedDaysWithCityNotFound() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(
        "NonExistentCityXYZ123", 7);

    assertNotNull(recommendation);
    assertNotNull(recommendation.getMessage());
    assertTrue(recommendation.getMessage().contains("Error") 
        || recommendation.getMessage().contains("Could not find coordinates")
        || recommendation.getMessage().contains("Error processing forecast"),
        "Should handle city not found gracefully");
  }

  @Test
  public void testGetRecommendedDaysExceptionHandling() {
    WeatherRecommendation recommendation = WeatherModel.getRecommendedDays("", 7);

    assertNotNull(recommendation);
    assertNotNull(recommendation.getMessage());
    assertTrue(recommendation.getMessage().contains("Error") 
        || recommendation.getMessage().contains("Could not find coordinates")
        || recommendation.getMessage().contains("Error processing forecast"),
        "Should handle empty city gracefully");
  }
}

