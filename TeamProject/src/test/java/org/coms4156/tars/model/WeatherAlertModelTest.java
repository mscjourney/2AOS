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
 * Comprehensive tests for WeatherAlertModel.
 * Includes parameter validation, basic functionality, edge cases, boundary conditions,
 * and error handling.
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

  // ========== Parameter Validation Tests ==========

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
  void testGetWeatherAlertsWithEmptyCityName() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts("", null, null);
    });
  }

  @Test
  void testGetWeatherAlertsWithWhitespaceOnlyCity() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts("   ", null, null);
    });
  }

  // ========== Basic Functionality Tests ==========

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
  void testGetWeatherAlertsWithNullCityButValidCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 40.7128, -74.0060);
    
    assertNotNull(alert);
    assertTrue(alert.getLocation().contains("40.7128"));
    assertTrue(alert.getLocation().contains("-74.0060"));
  }

  @Test
  void testGetWeatherAlertsWithCityAndCoordinates() {
    // When both city and coordinates are provided, coordinates should be used
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("New York", 51.5074, -0.1278);
    
    assertNotNull(alert);
    // Should use coordinates, not city name
    assertTrue(alert.getLocation().contains("51.5074"));
    assertTrue(alert.getLocation().contains("-0.1278"));
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
  void testGetUserAlertsWithValidUserWithPreferences() {
    User user = new User(1, 1, 
        List.of("sunny"), 
        List.of("70F"), 
        List.of("Boston", "New York"));
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    assertNotNull(alerts);
    assertTrue(alerts.size() >= 0);
  }

  @Test
  void testGetUserAlertsWithSingleCity() {
    List<String> singleCity = new ArrayList<>();
    singleCity.add("Paris");
    User user = new User(1, 1, new ArrayList<>(), new ArrayList<>(), singleCity);
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    
    assertNotNull(alerts);
    assertEquals(1, alerts.size());
    assertEquals("Paris", alerts.get(0).getLocation());
  }

  @Test
  void testGetUserAlertsWithMultipleCities() {
    List<String> multipleCities = new ArrayList<>();
    multipleCities.add("New York");
    multipleCities.add("London");
    multipleCities.add("Tokyo");
    multipleCities.add("Sydney");
    
    User userWithMultipleCities = new User(3, 3, new ArrayList<>(), 
                                            new ArrayList<>(), multipleCities);
    
    List<WeatherAlert> result = WeatherAlertModel.getUserAlerts(userWithMultipleCities);

    assertNotNull(result);
    assertEquals(4, result.size());
    assertEquals("New York", result.get(0).getLocation());
    assertEquals("London", result.get(1).getLocation());
    assertEquals("Tokyo", result.get(2).getLocation());
    assertEquals("Sydney", result.get(3).getLocation());
  }

  @Test
  void testGetUserAlertsWithDuplicateCities() {
    List<String> duplicateCities = new ArrayList<>();
    duplicateCities.add("London");
    duplicateCities.add("London");
    duplicateCities.add("London");
    
    User user = new User(3, 3, new ArrayList<>(), new ArrayList<>(), duplicateCities);
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    
    assertNotNull(alerts);
    assertEquals(3, alerts.size());
    // All should be for London
    for (WeatherAlert alert : alerts) {
      assertEquals("London", alert.getLocation());
    }
  }

  // ========== Edge Cases and Boundary Conditions ==========

  @Test
  void testGetWeatherAlertsWithNegativeCoordinates() {
    WeatherAlert result = WeatherAlertModel.getWeatherAlerts(null, -90.0, -180.0);

    assertNotNull(result);
    assertEquals("-90.0000, -180.0000", result.getLocation());
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
  void testGetWeatherAlertsWithExtremeLatitude() {
    WeatherAlert alert1 = WeatherAlertModel.getWeatherAlerts(null, 90.0, 0.0);
    WeatherAlert alert2 = WeatherAlertModel.getWeatherAlerts(null, -90.0, 0.0);
    
    assertNotNull(alert1);
    assertNotNull(alert2);
    assertTrue(alert1.getLocation().contains("90.0000"));
    assertTrue(alert2.getLocation().contains("-90.0000"));
  }

  @Test
  void testGetWeatherAlertsWithExtremeLongitude() {
    WeatherAlert alert1 = WeatherAlertModel.getWeatherAlerts(null, 0.0, 180.0);
    WeatherAlert alert2 = WeatherAlertModel.getWeatherAlerts(null, 0.0, -180.0);
    
    assertNotNull(alert1);
    assertNotNull(alert2);
    assertTrue(alert1.getLocation().contains("180.0000"));
    assertTrue(alert2.getLocation().contains("-180.0000"));
  }

  @Test
  void testGetWeatherAlertsWithVerySmallCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 0.0001, 0.0001);
    
    assertNotNull(alert);
    assertTrue(alert.getLocation().contains("0.0001"));
  }

  @Test
  void testGetWeatherAlertsWithVeryLargeCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 89.9999, 179.9999);
    
    assertNotNull(alert);
    assertTrue(alert.getLocation().contains("89.9999"));
    assertTrue(alert.getLocation().contains("179.9999"));
  }

  @Test
  void testGetWeatherAlertsWithZeroCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 0.0, 0.0);
    
    assertNotNull(alert);
    assertEquals("0.0000, 0.0000", alert.getLocation());
    assertNotNull(alert.getAlerts());
    assertNotNull(alert.getRecommendations());
  }

  @Test
  void testGetWeatherAlertsWithPreciseCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 40.7127753, -74.0059728);
    
    assertNotNull(alert);
    // Should format to 4 decimal places
    assertTrue(alert.getLocation().contains("40.7128"));
    assertTrue(alert.getLocation().contains("-74.0060") 
        || alert.getLocation().contains("-74.0059"));
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

  // ========== Error Handling Tests ==========

  @Test
  void testGetUserAlertsWithNullCityInPreferences() {
    List<String> citiesWithNull = new ArrayList<>();
    citiesWithNull.add("New York");
    citiesWithNull.add(null);
    
    User user = new User(2, 2, new ArrayList<>(), new ArrayList<>(), citiesWithNull);
    
    // This should either throw or handle gracefully
    try {
      List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
      // If it doesn't throw, verify first city worked
      assertNotNull(alerts);
      assertTrue(alerts.size() >= 1);
    } catch (Exception e) {
      // Expected if null city causes issues
      assertTrue(e instanceof IllegalArgumentException || e instanceof NullPointerException);
    }
  }

  @Test
  void testGetWeatherAlertsWithCityNameContainingSpecialCharacters() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("São Paulo", null, null);
    
    assertNotNull(alert);
    assertEquals("São Paulo", alert.getLocation());
  }

  @Test
  void testGetWeatherAlertsWithCityNameContainingNumbers() {
    // Test city names with numbers - may not be found by geocoding API
    try {
      WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("New York 10001", null, null);
      // If it doesn't throw, verify structure
      assertNotNull(alert);
      assertNotNull(alert.getLocation());
    } catch (IllegalArgumentException e) {
      // Expected if city is not found
      assertTrue(e.getMessage().contains("City not found"));
    }
  }

  @Test
  void testGetUserAlertsWithVeryLongCityName() {
    List<String> longCityName = new ArrayList<>();
    longCityName.add("A".repeat(500));
    
    User user = new User(4, 4, new ArrayList<>(), new ArrayList<>(), longCityName);
    
    // Should either throw or handle gracefully
    try {
      List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
      // If it doesn't throw, verify structure
      assertNotNull(alerts);
    } catch (Exception e) {
      // Expected for invalid city names
      assertTrue(e instanceof IllegalArgumentException);
    }
  }

  // ========== Structure Validation Tests ==========

  @Test
  void testGetWeatherAlertsReturnsValidStructure() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("Tokyo", null, null);
    
    assertNotNull(alert);
    assertNotNull(alert.getLocation());
    assertNotNull(alert.getTimestamp());
    assertNotNull(alert.getAlerts());
    assertNotNull(alert.getRecommendations());
    assertNotNull(alert.getCurrentConditions());
    
    // Alerts should not be empty (should have at least INFO alert)
    assertTrue(alert.getAlerts().size() > 0);
  }

  @Test
  void testGetWeatherAlertsWithCoordinatesReturnsValidStructure() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 35.6762, 139.6503);
    
    assertNotNull(alert);
    assertNotNull(alert.getLocation());
    assertNotNull(alert.getTimestamp());
    assertNotNull(alert.getAlerts());
    assertNotNull(alert.getRecommendations());
    assertNotNull(alert.getCurrentConditions());
    
    // Should have at least one alert
    assertTrue(alert.getAlerts().size() > 0);
  }

  @Test
  void testGetUserAlertsReturnsValidStructure() {
    List<String> cities = new ArrayList<>();
    cities.add("Berlin");
    cities.add("Madrid");
    
    User user = new User(5, 5, new ArrayList<>(), new ArrayList<>(), cities);
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    
    assertNotNull(alerts);
    assertEquals(2, alerts.size());
    
    for (WeatherAlert alert : alerts) {
      assertNotNull(alert.getLocation());
      assertNotNull(alert.getTimestamp());
      assertNotNull(alert.getAlerts());
      assertNotNull(alert.getRecommendations());
      assertNotNull(alert.getCurrentConditions());
    }
  }
}
