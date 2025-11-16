package org.coms4156.tars.model;

import java.util.List;

/**
 * City Summary object. Stores city information, weather recommendations,
 * weather alerts, and user preferences for the city.
 */
public class CitySummary {
  private String city;
  private WeatherRecommendation weatherRecommendation;
  private WeatherAlert weatherAlert;
  private List<User> interestedUsers;
  private String message;

 
  public CitySummary(String city, WeatherRecommendation weatherRecommendation,
                     WeatherAlert weatherAlert, List<User> interestedUsers, String message) {
    this.city = city;
    this.weatherRecommendation = weatherRecommendation;
    this.weatherAlert = weatherAlert;
    this.interestedUsers = interestedUsers;
    this.message = message;
  }

  /**
   *  constructor
   */
  public CitySummary() {
    this.city = "";
    this.weatherRecommendation = null;
    this.weatherAlert = null;
    this.interestedUsers = null;
    this.message = "";
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public WeatherRecommendation getWeatherRecommendation() {
    return weatherRecommendation;
  }

  public void setWeatherRecommendation(WeatherRecommendation weatherRecommendation) {
    this.weatherRecommendation = weatherRecommendation;
  }

  public WeatherAlert getWeatherAlert() {
    return weatherAlert;
  }

  public void setWeatherAlert(WeatherAlert weatherAlert) {
    this.weatherAlert = weatherAlert;
  }

  public List<User> getInterestedUsers() {
    return interestedUsers;
  }

  public void setInterestedUsers(List<User> interestedUsers) {
    this.interestedUsers = interestedUsers;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "CitySummary {"
            + "city='" + city + '\''
            + ", weatherRecommendation=" + weatherRecommendation
            + ", weatherAlert=" + weatherAlert
            + ", interestedUsers=" + interestedUsers
            + ", message='" + message + '\''
            + '}';
  }
}

