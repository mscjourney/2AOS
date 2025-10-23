package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests parameter validation and basic functionality
 */
public class WeatherAlertModelTest {

  private User testUser;

  @BeforeEach
  void setUp() {
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("London");
    testUser = new User(1, new ArrayList<>(), new ArrayList<>(), cityPreferences);
  }

  @Test
  void testGetWeatherAlertsWithNoParameters() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts(null, null, null);
    });
  }

  @Test
  void testGetWeatherAlertsWithOnlyLatitude() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts(null, 40.7128, null);
    });
  }

  @Test
  void testGetWeatherAlertsWithOnlyLongitude() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts(null, null, -74.0060);
    });
  }

  @Test
  void testGetUserAlertsWithNullUser() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getUserAlerts(null);
    });
  }

  @Test
  void testGetUserAlertsWithEmptyCityPreferences() {
    User userWithNoCities = new User(2, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    
    List<WeatherAlert> result = WeatherAlertModel.getUserAlerts(userWithNoCities);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testGetUserAlertsWithValidUser() {
    List<WeatherAlert> result = WeatherAlertModel.getUserAlerts(testUser);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("New York", result.get(0).getLocation());
    assertEquals("London", result.get(1).getLocation());
  }

  @Test
  void testGetWeatherAlertsWithValidCoordinates() {
    WeatherAlert result = WeatherAlertModel.getWeatherAlerts(null, 40.7128, -74.0060);

    assertNotNull(result);
    assertEquals("40.7128, -74.0060", result.getLocation());
    assertNotNull(result.getTimestamp());
    assertNotNull(result.getAlerts());
    assertNotNull(result.getRecommendations());
    assertNotNull(result.getCurrentConditions());
  }


  @Test
  void testGetWeatherAlertsWithNegativeCoordinates() {
    WeatherAlert result = WeatherAlertModel.getWeatherAlerts(null, -90.0, -180.0);

    assertNotNull(result);
    assertEquals("-90.0000, -180.0000", result.getLocation());
  }
}
