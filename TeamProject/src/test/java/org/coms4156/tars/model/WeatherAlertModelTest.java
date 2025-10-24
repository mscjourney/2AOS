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
 * Tests parameter validation and basic functionality.
 */
public class WeatherAlertModelTest {

  private User testUser;

  @BeforeEach
  void setUp() {
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("London");
    testUser = new User(1, 1, new ArrayList<>(), new ArrayList<>(), cityPreferences);
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
    User userWithNoCities = new User(2, 2, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    
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

  @Test
  void testGetWeatherAlertsWithCityParameter() {
    WeatherAlert result = WeatherAlertModel.getWeatherAlerts("New York", null, null);

    assertNotNull(result);
    assertEquals("New York", result.getLocation());
    assertNotNull(result.getTimestamp());
    assertNotNull(result.getAlerts());
    assertNotNull(result.getRecommendations());
    assertNotNull(result.getCurrentConditions());
  }

  @Test
  void testGetWeatherAlertsWithExtremeCoordinateValues() {
    WeatherAlert result1 = WeatherAlertModel.getWeatherAlerts(null, 90.0, 180.0);
    assertNotNull(result1);
    assertEquals("90.0000, 180.0000", result1.getLocation());

    WeatherAlert result2 = WeatherAlertModel.getWeatherAlerts(null, -90.0, -180.0);
    assertNotNull(result2);
    assertEquals("-90.0000, -180.0000", result2.getLocation());

    WeatherAlert result3 = WeatherAlertModel.getWeatherAlerts(null, 0.0, 0.0);
    assertNotNull(result3);
    assertEquals("0.0000, 0.0000", result3.getLocation());
  }

  @Test
  void testGetUserAlertsWithMultipleCities() {
    List<String> multipleCities = new ArrayList<>();
    multipleCities.add("New York");
    multipleCities.add("London");
    multipleCities.add("Tokyo");
    multipleCities.add("Sydney");
    
    User userWithMultipleCities = new User(3, 3, new ArrayList<>(), new ArrayList<>(), multipleCities);
    
    List<WeatherAlert> result = WeatherAlertModel.getUserAlerts(userWithMultipleCities);

    assertNotNull(result);
    assertEquals(4, result.size());
    assertEquals("New York", result.get(0).getLocation());
    assertEquals("London", result.get(1).getLocation());
    assertEquals("Tokyo", result.get(2).getLocation());
    assertEquals("Sydney", result.get(3).getLocation());
  }


  @Test
  void testGetWeatherAlertsBoundaryConditions() {
    WeatherAlert result1 = WeatherAlertModel.getWeatherAlerts(null, 40.7128, -74.0060);
    WeatherAlert result2 = WeatherAlertModel.getWeatherAlerts(null, 40.7129, -74.0061);
    
    assertNotNull(result1);
    assertNotNull(result2);
    assertTrue(!result1.getLocation().equals(result2.getLocation()));

    WeatherAlert result3 = WeatherAlertModel.getWeatherAlerts(null, 89.9999, 179.9999);
    assertNotNull(result3);
    assertEquals("89.9999, 179.9999", result3.getLocation());
  }
}
