package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the WeatherRecommendation model class.
 */
public class WeatherRecommendationTest {

  @Test
  void constructorTest() {
    List<String> days = Arrays.asList("2025-03-01", "2025-03-02");
    WeatherRecommendation recommendation =
            new WeatherRecommendation("New York", days, "Clear skies expected on these days.");

    assertEquals("New York", recommendation.getCity());
    assertEquals(days, recommendation.getRecommendedDays());
    assertEquals("Clear skies expected on these days.", recommendation.getMessage());
  }

  @Test
  void settersGettersTest() {
    WeatherRecommendation recommendation = new WeatherRecommendation();
    List<String> days = Arrays.asList("2025-04-10", "2025-04-11");

    recommendation.setCity("Los Angeles");
    recommendation.setRecommendedDays(days);
    recommendation.setMessage("Best days for outdoor activities.");

    assertEquals("Los Angeles", recommendation.getCity());
    assertEquals(days, recommendation.getRecommendedDays());
    assertEquals("Best days for outdoor activities.", recommendation.getMessage());
  }

  @Test
  void toStringTest() {
    List<String> days = Arrays.asList("2025-05-15", "2025-05-16");
    WeatherRecommendation recommendation =
            new WeatherRecommendation("Chicago", days, "Warm and sunny weekend ahead.");

    String result = recommendation.toString();

    assertTrue(result.contains("Chicago"));
    assertTrue(result.contains("2025-05-15"));
    assertTrue(result.contains("2025-05-16"));
    assertTrue(result.contains("Warm and sunny weekend ahead."));
    assertTrue(result.startsWith("WeatherRecommendation"));
  }
}
