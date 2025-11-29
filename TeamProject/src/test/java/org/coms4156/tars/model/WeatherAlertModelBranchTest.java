package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * This class contains branch coverage tests for WeatherAlertModel.
 */
public class WeatherAlertModelBranchTest {

  /**
   * {@code testGetWeatherAlertsWithCoordinates} Tests retrieving weather alerts
   * using coordinates.
   */
  @Test
  public void testGetWeatherAlertsWithCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 40.7128, -74.0060);
    assertNotNull(alert);
    assertNotNull(alert.getLocation());
    assertTrue(alert.getLocation().contains("40.7128"));
  }

  /**
   * {@code testGetWeatherAlertsWithCity} Tests retrieving weather alerts
   * using city name.
   */
  @Test
  public void testGetWeatherAlertsWithCity() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("New York", null, null);
    assertNotNull(alert);
    assertEquals("New York", alert.getLocation());
  }

  /**
   * {@code testGetWeatherAlertsThrowsWhenNoLocation} Tests that an exception
   * is thrown when no location is provided.
   */
  @Test
  public void testGetWeatherAlertsThrowsWhenNoLocation() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts(null, null, null);
    });
  }

  /**
   * {@code testGetUserAlertsWithNullUser} Tests that an exception is thrown
   * when user is null.
   */
  @Test
  public void testGetUserAlertsWithNullUser() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getUserAlerts(null);
    });
  }

  /**
   * {@code testGetUserAlertsWithValidUser} Tests retrieving alerts for a valid
   * user with city preferences.
   */
  @Test
  public void testGetUserAlertsWithValidUser() {
    UserPreference user = new UserPreference(1L, List.of("sunny"), List.of("70F"), 
        List.of("Boston", "New York"));
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    assertNotNull(alerts);
    assertTrue(alerts.size() >= 0);
  }

  /**
   * {@code testGetUserAlertsWithEmptyCityPreferences} Tests retrieving alerts
   * for a user with empty city preferences.
   */
  @Test
  public void testGetUserAlertsWithEmptyCityPreferences() {
    UserPreference user = new UserPreference(1L, List.of("sunny"), List.of("70F"), List.of());
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    assertNotNull(alerts);
    assertTrue(alerts.isEmpty());
  }
}

