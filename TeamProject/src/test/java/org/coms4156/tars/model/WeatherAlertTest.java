package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests all constructors, getters, setters, and the toString method.
 */
public class WeatherAlertTest {

  private List<Map<String, String>> sampleAlerts;
  private List<String> sampleRecommendations;
  private Map<String, Object> sampleCurrentConditions;

  @BeforeEach
  void setUp() {
    sampleAlerts = new ArrayList<>();
    Map<String, String> alert = new HashMap<>();
    alert.put("severity", "HIGH");
    alert.put("type", "HEAT");
    alert.put("message", "Extreme heat advisory");
    sampleAlerts.add(alert);

    sampleRecommendations = new ArrayList<>();
    sampleRecommendations.add("Stay hydrated");
    sampleRecommendations.add("Avoid outdoor activities");
    sampleCurrentConditions = new HashMap<>();
    sampleCurrentConditions.put("temperature_celsius", 38.5);
    sampleCurrentConditions.put("humidity_percent", 70.0);
    sampleCurrentConditions.put("wind_speed_kmh", 25.0);
    sampleCurrentConditions.put("precipitation_mm", 0.0);
    sampleCurrentConditions.put("weather_code", 1);
  }

  @Test
  void testDefaultConstructor() {
    WeatherAlert weatherAlert = new WeatherAlert();
    
    assertNotNull(weatherAlert);
    assertNull(weatherAlert.getLocation());
    assertNull(weatherAlert.getTimestamp());
    assertNull(weatherAlert.getAlerts());
    assertNull(weatherAlert.getRecommendations());
    assertNull(weatherAlert.getCurrentConditions());
  }

  @Test
  void testParameterizedConstructor() {
    String location = "New York";
    
    WeatherAlert weatherAlert = new WeatherAlert(
        location, 
        sampleAlerts, 
        sampleRecommendations, 
        sampleCurrentConditions
    );
    
    assertNotNull(weatherAlert);
    assertEquals(location, weatherAlert.getLocation());
    assertNotNull(weatherAlert.getTimestamp());
    assertEquals(sampleAlerts, weatherAlert.getAlerts());
    assertEquals(sampleRecommendations, weatherAlert.getRecommendations());
    assertEquals(sampleCurrentConditions, weatherAlert.getCurrentConditions());
  }

  @Test
  void testParameterizedConstructorWithNullValues() {
    WeatherAlert weatherAlert = new WeatherAlert(
        null, 
        null, 
        null, 
        null
    );
    
    assertNotNull(weatherAlert);
    assertNull(weatherAlert.getLocation());
    assertNotNull(weatherAlert.getTimestamp()); 
    assertNull(weatherAlert.getRecommendations());
    assertNull(weatherAlert.getCurrentConditions());
  }

  @Test
  void testLocationGetterAndSetter() {
    WeatherAlert weatherAlert = new WeatherAlert();
    
    String location = "San Francisco";
    weatherAlert.setLocation(location);
    assertEquals(location, weatherAlert.getLocation());
    
    weatherAlert.setLocation(null);
    assertNull(weatherAlert.getLocation());
    
    weatherAlert.setLocation("");
    assertEquals("", weatherAlert.getLocation());
  }

  @Test
  void testTimestampGetterAndSetter() {
    WeatherAlert weatherAlert = new WeatherAlert();
    
    // Test setter
    String timestamp = "2024-01-15T10:30:00Z";
    weatherAlert.setTimestamp(timestamp);
    assertEquals(timestamp, weatherAlert.getTimestamp());
    
    // Test with null
    weatherAlert.setTimestamp(null);
    assertNull(weatherAlert.getTimestamp());
    
    // Test with empty string
    weatherAlert.setTimestamp("");
    assertEquals("", weatherAlert.getTimestamp());
  }

  @Test
  void testAlertsGetterAndSetter() {
    WeatherAlert weatherAlert = new WeatherAlert();
    
    // Test setter
    weatherAlert.setAlerts(sampleAlerts);
    assertEquals(sampleAlerts, weatherAlert.getAlerts());
    
    // Test with null
    weatherAlert.setAlerts(null);
    assertNull(weatherAlert.getAlerts());
    
    // Test with empty list
    List<Map<String, String>> emptyAlerts = new ArrayList<>();
    weatherAlert.setAlerts(emptyAlerts);
    assertEquals(emptyAlerts, weatherAlert.getAlerts());
    assertTrue(weatherAlert.getAlerts().isEmpty());
  }

  @Test
  void testRecommendationsGetterAndSetter() {
    WeatherAlert weatherAlert = new WeatherAlert();
    
    // Test setter
    weatherAlert.setRecommendations(sampleRecommendations);
    assertEquals(sampleRecommendations, weatherAlert.getRecommendations());
    
    // Test with null
    weatherAlert.setRecommendations(null);
    assertNull(weatherAlert.getRecommendations());
    
    // Test with empty list
    List<String> emptyRecommendations = new ArrayList<>();
    weatherAlert.setRecommendations(emptyRecommendations);
    assertEquals(emptyRecommendations, weatherAlert.getRecommendations());
    assertTrue(weatherAlert.getRecommendations().isEmpty());
  }

  @Test
  void testToString() {
    WeatherAlert weatherAlert = new WeatherAlert(
        "London", 
        sampleAlerts, 
        sampleRecommendations, 
        sampleCurrentConditions
    );
    
    String result = weatherAlert.toString();
    
    assertNotNull(result);
    assertTrue(result.contains("WeatherAlert{"));
    assertTrue(result.contains("location='London'"));
    assertTrue(result.contains("timestamp="));
    assertTrue(result.contains("alerts="));
    assertTrue(result.contains("recommendations="));
    assertTrue(result.contains("currentConditions="));
    assertTrue(result.endsWith("}"));
  }

  @Test
  void testToStringWithNullValues() {
    WeatherAlert weatherAlert = new WeatherAlert();
    weatherAlert.setLocation("Paris");
    
    String result = weatherAlert.toString();
    
    assertNotNull(result);
    assertTrue(result.contains("WeatherAlert{"));
    assertTrue(result.contains("location='Paris'"));
    assertTrue(result.contains("timestamp=")); 
    assertTrue(result.contains("alerts=null"));
    assertTrue(result.contains("recommendations=null"));
    assertTrue(result.contains("currentConditions=null"));
  }
}
