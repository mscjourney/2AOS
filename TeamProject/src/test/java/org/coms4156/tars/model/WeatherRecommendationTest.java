package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the WeatherRecommendation model class.
 */
public class WeatherRecommendationTest {

  @Test
  void constructorTest_basic() {
    List<String> days = Arrays.asList("2025-03-01", "2025-03-02");
    WeatherRecommendation recommendation =
            new WeatherRecommendation("New York", days, "Clear skies expected on these days.");

    assertEquals("New York", recommendation.getCity());
    assertEquals(days, recommendation.getRecommendedDays());
    assertEquals("Clear skies expected on these days.", recommendation.getMessage());
    assertEquals(0.0, recommendation.getMinTemperature());
    assertEquals(0.0, recommendation.getMaxTemperature());
  }

  @Test
  void constructorTest_withTemperatures() {
    List<String> days = Arrays.asList("2025-04-05", "2025-04-06");
    WeatherRecommendation recommendation =
            new WeatherRecommendation("Miami", days, "Warm weather expected.", 72.5, 88.3);

    assertEquals("Miami", recommendation.getCity());
    assertEquals(days, recommendation.getRecommendedDays());
    assertEquals("Warm weather expected.", recommendation.getMessage());
    assertEquals(72.5, recommendation.getMinTemperature());
    assertEquals(88.3, recommendation.getMaxTemperature());
  }

  @Test
  void settersGettersTest() {
    WeatherRecommendation recommendation = new WeatherRecommendation();
    List<String> days = Arrays.asList("2025-04-10", "2025-04-11");

    recommendation.setCity("Los Angeles");
    recommendation.setRecommendedDays(days);
    recommendation.setMessage("Best days for outdoor activities.");
    recommendation.setMinTemperature(55.0);
    recommendation.setMaxTemperature(75.0);

    assertEquals("Los Angeles", recommendation.getCity());
    assertEquals(days, recommendation.getRecommendedDays());
    assertEquals("Best days for outdoor activities.", recommendation.getMessage());
    assertEquals(55.0, recommendation.getMinTemperature());
    assertEquals(75.0, recommendation.getMaxTemperature());
  }

  @Test
  void toStringTest() {
    List<String> days = Arrays.asList("2025-05-15", "2025-05-16");
    WeatherRecommendation recommendation =
            new WeatherRecommendation("Chicago", days,
                    "Warm and sunny weekend ahead.", 60.0, 82.0);

    String result = recommendation.toString();

    assertTrue(result.contains("Chicago"));
    assertTrue(result.contains("2025-05-15"));
    assertTrue(result.contains("2025-05-16"));
    assertTrue(result.contains("Warm and sunny weekend ahead."));
    assertTrue(result.contains("60.0"));
    assertTrue(result.contains("82.0"));
    assertTrue(result.startsWith("WeatherRecommendation"));
  }

  @Test
  public void testGetUserRecDaysWithValidCityAndPreferences() {
    UserPreference user = new UserPreference(
            123L,
            List.of("clear"),
            List.of("10", "15", "20"),  // Celsius preferences
            List.of("New York")
    );

    WeatherRecommendation recommendation =
            WeatherModel.getUserRecDays("New York", 7, user);

    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("New York", recommendation.getCity(), "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), "Recommended days should not be null");
    assertNotNull(recommendation.getMessage(), "Message should not be null");

    assertTrue(
            recommendation.getMessage().contains("Recommended")
                    || recommendation.getMessage().contains("No days meet your preferences"),
            "Message should reflect whether preferences matched"
    );
  }

  @Test
  public void testGetUserRecDaysNoMatchingTemperaturePreferences() {
    // Extremely cold preferences that no city will hit in normal forecasts
    UserPreference user = new UserPreference(
            999L,
            List.of("clear"),
            List.of("-50", "-40"),  // Unlikely Celsius temperatures
            List.of("Miami")
    );

    WeatherRecommendation recommendation =
            WeatherModel.getUserRecDays("Miami", 5, user);

    assertNotNull(recommendation);
    assertTrue(
            recommendation.getRecommendedDays().isEmpty(),
            "Expected no recommended days with impossible Celsius preferences"
    );
    assertTrue(
            recommendation.getMessage().contains("No days meet your preferences"),
            "Message should indicate no match for temperature preferences"
    );
  }

  @Test
  public void testGetUserRecDaysWithInvalidCity() {
    UserPreference user = new UserPreference(
            42L,
            List.of("clear"),
            List.of("10", "20"),  // reasonable Celsius temps
            List.of("InvalidCity")
    );

    String invalidCity = "NonExistentCityXYZ999";

    WeatherRecommendation recommendation =
            WeatherModel.getUserRecDays(invalidCity, 7, user);

    assertNotNull(recommendation);
    assertEquals(invalidCity, recommendation.getCity(), "City should match input even if invalid");

    assertTrue(
            recommendation.getMessage().contains("Error")
                    || recommendation.getMessage().contains("Could not find")
                    || recommendation.getMessage().contains("Error processing forecast"),
            "Should indicate error for invalid city"
    );
  }

}