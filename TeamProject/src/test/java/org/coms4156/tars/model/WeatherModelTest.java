package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * {@code WeatherModelTest} Unit tests for WeatherModel class.
 * 
 * <p>Equivalence Partitions for WeatherModel Methods
 * ======== {@code WeatherRecommendation getRecommendedDays(String city, int days)} ========
 * 1) Equivalence Partition 1: city is valid and days is in the range [1, 14] inclusively.
 *    - Test Cases: testGetRecommendedDaysWithValidCity, testGetRecommendedDaysWithValidCityAndDays,
 *        testGetRecommendedDaysWithCityContainingSpaces, testGetRecommendedDaysWithSingleDay,
 *        testGetRecommendedDaysWithMaximumDays, testGetRecommendedDaysWithSpecialCharactersInCity,
 *        testGetRecommendedDaysWithDifferentDayCounts
 * 2) Equivalence Partition 2: days is out of the range
 *    - Test Cases: testGetRecommendedDaysOutOfRange
 * 3) Equivalence Partition 3: city is invalid
 *    - Test Cases: testGetRecommendedDaysWithInvalidCity, testGetRecommendedDaysWithEmptyCityName
 * === {@code WeatherRecommendation getUserRecDays(String city, int days, UserPreference user)} ===
 * Method is only called after confirming UserPreference exists from entry point. 
 * 1) Equivalence Partition 1: city is valid and days is in the range [1, 14] inclusively.
 *      temperaturePreferences is NOT empty.
 *    - Test Cases: testGetUserRecommendedDaysValidNotEmptyPreferences
 * 2) Equivalence Partition 2: city is valid and days is in the range [1, 14] inclusively.
 *      temperaturePreferences IS empty.
 *    - Test Cases: testGetUserRecommendedDaysEmptyPreferences
 * 3) Equivalence Partition 3: days is out of the range
 *    - Test Cases: testGetUserRecommendedDaysWithInvalidCity,
 *        testGetUserRecommendedDaysWithEmptyCityName
 * 4) Equivalence Partition 4: city is invalid
 *    - Test Cases: testGetUserRecommendedDaysOutOfRange
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
    
    assertTrue(hasClearDaysMessage || hasNoClearDaysMessage, 
        "Message should indicate clear days, no clear days");
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
  public void testGetRecommendedDaysWithDifferentDayCounts() {
    // Test that the method works with different day counts
    for (int days = 1; days <= 14; days++) {
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
  public void testGetRecommendedDaysWithInvalidCity() {
    String invalidCity = "NonExistentCity12345XYZ";

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getRecommendedDays(invalidCity, 7);
    });

    assertTrue(exception.getMessage().contains("City Not Found"));
  }

  @Test
  public void testGetRecommendedDaysWithEmptyCityName() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getRecommendedDays(" ", 7);
    });

    assertTrue(exception.getMessage().contains("City Not Found"));
  }

  @Test
  public void testGetRecommendedDaysOutOfRange() {
    IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getRecommendedDays(" ", 0);
    });

    assertTrue(exception1.getMessage().contains("Days out of range"));

    IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getRecommendedDays(" ", 15);
    });

    assertTrue(exception2.getMessage().contains("Days out of range"));

    IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getRecommendedDays(" ", 120);
    });

    assertTrue(exception3.getMessage().contains("Days out of range"));
  }

  @Test
  public void testGetUserRecommendedDaysValidNotEmptyPreferences() {
    List<String> tempPrefs = List.of("13", "17", "20");
    UserPreference newPreference = new UserPreference(1L, List.of(), tempPrefs, List.of());

    WeatherRecommendation recommendation = WeatherModel.getUserRecDays("Boston", 5, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), "Message should not be null");
    
    // Lower Boundary
    recommendation = WeatherModel.getUserRecDays("Boston", 1, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), "Message should not be null");
    // Upper Boundary
    recommendation = WeatherModel.getUserRecDays("Boston", 14, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), "Recommended days list should not be null");
    assertNotNull(recommendation.getMessage(), "Message should not be null");
  }

  @Test
  public void testGetUserRecommendedDaysEmptyPreferences() {
    UserPreference newPreference = new UserPreference(1L, List.of(), List.of(), List.of());

    WeatherRecommendation recommendation = WeatherModel.getUserRecDays("Boston", 5, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    // Empty Preferences will never have any days that meet preference
    assertEquals(recommendation.getMessage(), 
        "No days meet your preferences in Boston over the next 5 days.");
    
    // Lower Boundary
    recommendation = WeatherModel.getUserRecDays("Boston", 1, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertEquals(recommendation.getMessage(), 
        "No days meet your preferences in Boston over the next 1 days.");

    // Upper Boundary
    recommendation = WeatherModel.getUserRecDays("Boston", 14, newPreference);
    
    assertNotNull(recommendation, "Recommendation should not be null");
    assertEquals("Boston", recommendation.getCity(), 
        "City should match input");
    assertNotNull(recommendation.getRecommendedDays(), 
        "Recommended days list should not be null");
    assertEquals(recommendation.getMessage(), 
        "No days meet your preferences in Boston over the next 14 days.");
  }

  @Test
  public void testGetUserRecommendedDaysWithInvalidCity() {
    String invalidCity = "NonExistentCity12345XYZ";

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getUserRecDays(invalidCity, 7, new UserPreference());
    });

    assertTrue(exception.getMessage().contains("City Not Found"));
  }

  @Test
  public void testGetUserRecommendedDaysWithEmptyCityName() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getUserRecDays(" ", 7, new UserPreference());
    });

    assertTrue(exception.getMessage().contains("City Not Found"));
  }

  @Test
  public void testGetUserRecommendedDaysOutOfRange() {
    IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getUserRecDays(" ", 0, new UserPreference());
    });

    assertTrue(exception1.getMessage().contains("Days out of range"));

    IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getUserRecDays(" ", 15, new UserPreference());
    });

    assertTrue(exception2.getMessage().contains("Days out of range"));

    IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
      WeatherModel.getUserRecDays(" ", 120, new UserPreference());
    });

    assertTrue(exception3.getMessage().contains("Days out of range"));
  }

}

