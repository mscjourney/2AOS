package org.coms4156.tars.model;

import java.util.List;

/**
 * Represents a simple weather recommendation for a given city.
 */
public class WeatherRecommendation {

  private String city;
  private List<String> recommendedDays;
  private String message;
  private double minTemperature;
  private double maxTemperature;

  /**
   * Creates a new {@code WeatherRecommendation} with the specified city,
   * list of recommended days, and descriptive message.
   *
   * @param city the name of the city for which the recommendation applies
   * @param recommendedDays a list of dates that have favorable or clear weather
   * @param message a descriptive message summarizing the weather recommendation
   */
  public WeatherRecommendation(String city, List<String> recommendedDays, String message) {
    this.city = city;
    this.recommendedDays = recommendedDays;
    this.message = message;
    this.minTemperature = 0;
    this.maxTemperature = 0;
  }

  /**
   * Creates a new {@code WeatherRecommendation} with the specified city,
   * list of recommended days, and descriptive message.
   *
   * @param city the name of the city for which the recommendation applies
   * @param recommendedDays a list of dates that have favorable or clear weather
   * @param message a descriptive message summarizing the weather recommendation
   * @param minTemperature for this time period
   * @param maxTemperaturegit  temperature for this time period
   */
  public WeatherRecommendation(String city, List<String> recommendedDays, String message,
                               double minTemperature, double maxTemperature) {
    this.city = city;
    this.recommendedDays = recommendedDays;
    this.message = message;
    this.minTemperature = minTemperature;
    this.maxTemperature = maxTemperature;
  }

  public WeatherRecommendation() {}

  // Getters and Setters
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public List<String> getRecommendedDays() {
    return recommendedDays;
  }

  public void setRecommendedDays(List<String> recommendedDays) {
    this.recommendedDays = recommendedDays;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public double getMinTemperature() {
    return minTemperature;
  }

  public void setMinTemperature(double minTemperature) {
    this.minTemperature = minTemperature;
  }

  public double getMaxTemperature() {
    return maxTemperature;
  }

  public void setMaxTemperature(double maxTemperature) {
    this.maxTemperature = maxTemperature;
  }

  // toString() for debugging
  @Override
  public String toString() {
    return "WeatherRecommendation{"
            + "city='" + city + '\''
            + ", recommendedDays=" + recommendedDays
            + ", message='" + message + '\''
            + ", minTemperature=" + minTemperature
            + ", maxTemperature=" + maxTemperature
            + '}';
  }
}
