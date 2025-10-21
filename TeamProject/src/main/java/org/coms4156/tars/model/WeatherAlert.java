package org.coms4156.tars.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Represents weather alert information for a given location.
 */
public class WeatherAlert {

  private String location;
  private String timestamp;
  private List<Map<String, String>> alerts;
  private List<String> recommendations;
  private Map<String, Object> currentConditions;

  /**
   * Creates a new WeatherAlert with the specified parameters.
   *
   * @param location the location name or coordinates
   * @param alerts list of alert maps containing severity, type, and message
   * @param recommendations list of recommendation strings
   * @param currentConditions map of current weather conditions
   */
  public WeatherAlert(String location, 
                      List<Map<String, String>> alerts,
                      List<String> recommendations,
                      Map<String, Object> currentConditions) {
    this.location = location;
    this.timestamp = new Date().toString();
    this.alerts = alerts;
    this.recommendations = recommendations;
    this.currentConditions = currentConditions;
  }

  public WeatherAlert() {}

  // Getters and Setters
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public List<Map<String, String>> getAlerts() {
    return alerts;
  }

  public void setAlerts(List<Map<String, String>> alerts) {
    this.alerts = alerts;
  }

  public List<String> getRecommendations() {
    return recommendations;
  }

  public void setRecommendations(List<String> recommendations) {
    this.recommendations = recommendations;
  }

  public Map<String, Object> getCurrentConditions() {
    return currentConditions;
  }

  public void setCurrentConditions(Map<String, Object> currentConditions) {
    this.currentConditions = currentConditions;
  }

  @Override
  public String toString() {
    return "WeatherAlert{"
            + "location='" + location + '\''
            + ", timestamp='" + timestamp + '\''
            + ", alerts=" + alerts
            + ", recommendations=" + recommendations
            + ", currentConditions=" + currentConditions
            + '}';
  }
}