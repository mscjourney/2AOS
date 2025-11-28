package org.coms4156.tars.model;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests parameter validation and basic functionality.
 */
public class WeatherAlertModelTest {

  private UserPreference testUser;

  @BeforeEach
  void setUp() {
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("London");
    testUser = new UserPreference(1L, new ArrayList<>(), new ArrayList<>(), cityPreferences);
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
    UserPreference userWithNoCities = new UserPreference(2L, new ArrayList<>(), 
                                                          new ArrayList<>(), new ArrayList<>());
    
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
    
    UserPreference userWithMultipleCities = new UserPreference(3L, new ArrayList<>(), 
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

  @Test
  void testGenerateEmptyJsonAlert() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
            weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);

    String emptyJson = """
      {
        "current": {}
      }
        """;

    List<Map<String, String>> alerts = 
            (List<Map<String, String>>) generateAlerts.invoke(null, emptyJson);
    Map<String, String> tempAlert = alerts.get(0);
    assertEquals(tempAlert.get("severity"), "INFO");
    assertEquals(tempAlert.get("type"), "CLEAR");
    assertEquals(tempAlert.get("message"), "No weather alerts at this time");
  }
  
  @Test
  void testGenerateNullJsonAlert() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
            weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);

    List<Map<String, String>> alerts = (List<Map<String, String>>) generateAlerts.invoke(null, "");
    Map<String, String> tempAlert = alerts.get(0);
    assertEquals(tempAlert.get("severity"), "ERROR");
    assertEquals(tempAlert.get("type"), "SYSTEM");
    assertEquals(tempAlert.get("message"), "Error processing weather data");
  }

  @Test
  void testGenerateAlertHighTemperature() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
            weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);
    
    String highTempJson = """
      {
        "current": {
          "temperature_2m": 40
        }
      }
        """;
    
    List<Map<String, String>> alerts1 = 
            (List<Map<String, String>>) generateAlerts.invoke(null, highTempJson);
    
    assertNotNull(alerts1); // Alerts should never be null
    Map<String, String> tempAlert1 = alerts1.get(0);
    assertEquals(tempAlert1.get("severity"), "HIGH");
    assertEquals(tempAlert1.get("type"), "HEAT");
    assertEquals(tempAlert1.get("message"), "Extreme heat advisory: Temperature above 35°C");

    String highTempThresholdJson = """
      {
        "current": {
          "temperature_2m": 35
        }
      }
        """;
    
    List<Map<String, String>> alerts2 = 
          (List<Map<String, String>>) generateAlerts.invoke(null, highTempThresholdJson);
    
    assertNotNull(alerts2); // Alerts should never be null
    Map<String, String> tempAlert2 = alerts2.get(0);
    assertEquals(tempAlert2.get("severity"), "INFO");
    assertEquals(tempAlert2.get("type"), "CLEAR");
    assertEquals(tempAlert2.get("message"), "No weather alerts at this time");
  }

  @Test
  void testGenerateAlertLowTemperature() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
            weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);
    
    String lowTempJson = """
      {
        "current": {
          "temperature_2m": -30
        }
      }
        """;
    
    List<Map<String, String>> alerts1 = 
          (List<Map<String, String>>) generateAlerts.invoke(null, lowTempJson);
    
    assertNotNull(alerts1); // Alerts should never be null
    Map<String, String> tempAlert1 = alerts1.get(0);
    assertEquals(tempAlert1.get("severity"), "MEDIUM");
    assertEquals(tempAlert1.get("type"), "COLD");
    assertEquals(tempAlert1.get("message"), "Freezing conditions: Temperature below 0°C");

    String lowTempThresholdJson = """
      {
        "current": {
          "temperature_2m": 0
        }
      }
        """;
    
    List<Map<String, String>> alerts2 = 
          (List<Map<String, String>>) generateAlerts.invoke(null, lowTempThresholdJson);
    
    assertNotNull(alerts2); // Alerts should never be null
    Map<String, String> tempAlert2 = alerts2.get(0);
    assertEquals(tempAlert2.get("severity"), "INFO");
    assertEquals(tempAlert2.get("type"), "CLEAR");
    assertEquals(tempAlert2.get("message"), "No weather alerts at this time");
  }

  @Test
  void testGenerateAlertHighWind() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts =
            weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);
    
    String highWindJson = """
      {
        "current": {
          "wind_speed_10m": 60
        }
      }
        """;
    
    List<Map<String, String>> alerts1 =
          (List<Map<String, String>>) generateAlerts.invoke(null, highWindJson);
    
    assertNotNull(alerts1); // Alerts should never be null
    Map<String, String> tempAlert1 = alerts1.get(0);
    assertEquals(tempAlert1.get("severity"), "HIGH");
    assertEquals(tempAlert1.get("type"), "WIND");
    assertEquals(tempAlert1.get("message"), "High wind warning: Wind speeds above 50 km/h");

    String highWindThresholdJson = """
      {
        "current": {
          "wind_speed_10m": 50
        }
      }
        """;
    
    List<Map<String, String>> alerts2 = 
          (List<Map<String, String>>) generateAlerts.invoke(null, highWindThresholdJson);
    
    assertNotNull(alerts2); // Alerts should never be null
    Map<String, String> tempAlert2 = alerts2.get(0);
    assertEquals(tempAlert2.get("severity"), "INFO");
    assertEquals(tempAlert2.get("type"), "CLEAR");
    assertEquals(tempAlert2.get("message"), "No weather alerts at this time");
  }

  @Test
  void testGenerateAlertPrecipitation() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
          weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);
    
    String highPrecipitationJson = """
      {
        "current": {
          "precipitation": 10
        }
      }
        """;
    
    List<Map<String, String>> alerts1 = 
            (List<Map<String, String>>) generateAlerts.invoke(null, highPrecipitationJson);
    
    assertNotNull(alerts1); // Alerts should never be null
    Map<String, String> tempAlert1 = alerts1.get(0);
    assertEquals(tempAlert1.get("severity"), "MEDIUM");
    assertEquals(tempAlert1.get("type"), "RAIN");
    assertEquals(tempAlert1.get("message"), "Heavy precipitation: Rain exceeding 5mm");

    String highPrecipitationThresholdJson = """
      {
        "current": {
           "precipitation": 5
        }
      }
        """;
    
    List<Map<String, String>> alerts2 = 
          (List<Map<String, String>>) generateAlerts.invoke(null, highPrecipitationThresholdJson);
    
    assertNotNull(alerts2); // Alerts should never be null
    Map<String, String> tempAlert2 = alerts2.get(0);
    assertEquals(tempAlert2.get("severity"), "INFO");
    assertEquals(tempAlert2.get("type"), "CLEAR");
    assertEquals(tempAlert2.get("message"), "No weather alerts at this time");
  }

  @Test
  void testGenerateAlertCombination() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateAlerts = 
          weatherAlertModelClass.getDeclaredMethod("generateAlerts", String.class);
    generateAlerts.setAccessible(true);
    
    String combinationJson = """
      {
        "current": {
          "temperature_2m": -20,
          "wind_speed_10m": 30,
          "precipitation": 10
        }
      }
        """;

    List<Map<String, String>> alerts = 
          (List<Map<String, String>>) generateAlerts.invoke(null, combinationJson);
    assertNotNull(alerts);

    Map<String, String> alertTemp = alerts.get(0);
    assertEquals(alertTemp.get("severity"), "MEDIUM");
    assertEquals(alertTemp.get("type"), "COLD");
    assertEquals(alertTemp.get("message"), "Freezing conditions: Temperature below 0°C");

    Map<String, String> alertPrecipitation = alerts.get(1);
    assertEquals(alertPrecipitation.get("severity"), "MEDIUM");
    assertEquals(alertPrecipitation.get("type"), "RAIN");
    assertEquals(alertPrecipitation.get("message"), "Heavy precipitation: Rain exceeding 5mm");
  }

  @Test
  void testGenerateRecommendationsEmptyJson() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateRecommendations = 
          weatherAlertModelClass.getDeclaredMethod("generateRecommendations", String.class);
    generateRecommendations.setAccessible(true);

    String emptyJson = """
      {
        "current": {}
      }
        """;

    List<String> recommendations = (List<String>) generateRecommendations.invoke(null, emptyJson);
    assertNotNull(recommendations);
    assertEquals(0, recommendations.size());
  }

  @Test
  void testGenerateRecommendationsNullJson() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateRecommendations = 
          weatherAlertModelClass.getDeclaredMethod("generateRecommendations", String.class);
    generateRecommendations.setAccessible(true);

    List<String> recommendations = (List<String>) generateRecommendations.invoke(null, "");
    assertNotNull(recommendations);
    assertEquals(1, recommendations.size());
    assertTrue(recommendations.contains("Unable to generate recommendations at this time"));
  }
  
  @Test
  void testGenerateRecommendationsPrecipitation() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateRecommendations = 
          weatherAlertModelClass.getDeclaredMethod("generateRecommendations", String.class);
    generateRecommendations.setAccessible(true);

    String precipitationJson = """
      {
        "current": {
          "precipitation": 10
        }
      }
        """;

    List<String> recommendations = 
          (List<String>) generateRecommendations.invoke(null, precipitationJson);
    assertNotNull(recommendations);
    assertEquals(2, recommendations.size());
    assertTrue(recommendations.contains("Bring an umbrella or raincoat"));
    assertTrue(recommendations.contains("Plan indoor activities"));

    String precipitationThresholdJson = """
      {
        "current": {
          "precipitation": 2
        }
      }
        """;

    List<String> recommendations2 = 
          (List<String>) generateRecommendations.invoke(null, precipitationThresholdJson);

    assertNotNull(recommendations);
    assertEquals(0, recommendations2.size());
  }

  @Test
  void testGenerateRecommendationsWind() throws Exception {
    Class<?> weatherAlertModelClass = Class.forName("org.coms4156.tars.model.WeatherAlertModel");
    Method generateRecommendations = 
          weatherAlertModelClass.getDeclaredMethod("generateRecommendations", String.class);
    generateRecommendations.setAccessible(true);

    String windJson = """
      {
        "current": {
          "wind_speed_10m": 50
        }
      }
        """;

    List<String> recommendations = (List<String>) generateRecommendations.invoke(null, windJson);

    assertNotNull(recommendations);
    assertEquals(1, recommendations.size());
    assertTrue(recommendations.contains("Secure loose items and avoid exposed areas"));

    String windThresholdJson = """
      {
        "current": {
          "wind_speed_10m": 30
        }
      }
        """;

    List<String> recommendations2 = 
          (List<String>) generateRecommendations.invoke(null, windThresholdJson);

    assertNotNull(recommendations2);
    assertEquals(0, recommendations2.size());
  }
}
