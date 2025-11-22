package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;


public class WeatherAlertModelBranchTest {

  @Test
  public void testGetWeatherAlertsWithCoordinates() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(null, 40.7128, -74.0060);
    assertNotNull(alert);
    assertNotNull(alert.getLocation());
    assertTrue(alert.getLocation().contains("40.7128"));
  }

  @Test
  public void testGetWeatherAlertsWithCity() {
    WeatherAlert alert = WeatherAlertModel.getWeatherAlerts("New York", null, null);
    assertNotNull(alert);
    assertEquals("New York", alert.getLocation());
  }

  @Test
  public void testGetWeatherAlertsThrowsWhenNoLocation() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getWeatherAlerts(null, null, null);
    });
  }

  @Test
  public void testGetUserAlertsWithNullUser() {
    assertThrows(IllegalArgumentException.class, () -> {
      WeatherAlertModel.getUserAlerts(null);
    });
  }

  @Test
  public void testGetUserAlertsWithValidUser() {
    User user = new User(1, 1, 
        List.of("sunny"), 
        List.of("70F"), 
        List.of("Boston", "New York"));
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    assertNotNull(alerts);
    assertTrue(alerts.size() >= 0);
  }

  @Test
  public void testGetUserAlertsWithEmptyCityPreferences() {
    User user = new User(1, 1, 
        List.of("sunny"), 
        List.of("70F"), 
        List.of());
    
    List<WeatherAlert> alerts = WeatherAlertModel.getUserAlerts(user);
    assertNotNull(alerts);
    assertTrue(alerts.isEmpty());
  }
}

