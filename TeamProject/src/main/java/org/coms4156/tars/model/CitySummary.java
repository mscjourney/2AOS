package org.coms4156.tars.model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * City Summary object. Stores city information, weather recommendations,
 * weather alerts, travel advisory, and user preferences for the city.
 */
public class CitySummary {
  private static final Logger logger = LoggerFactory.getLogger(CitySummary.class);
  private static final HttpClient httpClient = HttpClient.newHttpClient();

  private String city;
  private WeatherRecommendation weatherRecommendation;
  private WeatherAlert weatherAlert;
  private TravelAdvisory travelAdvisory;
  private List<User> interestedUsers;
  private String message;

  /**
   * {@code CitySummary} Constructs a CitySummary with the specified parameters.
   *
   * @param city the city name
   * @param weatherRecommendation the weather recommendation for the city
   * @param weatherAlert the weather alert for the city
   * @param travelAdvisory the travel advisory for the city
   * @param interestedUsers the list of users interested in the city
   * @param message the message associated with the city summary
   */
  public CitySummary(String city, WeatherRecommendation weatherRecommendation,
                     WeatherAlert weatherAlert, TravelAdvisory travelAdvisory,
                     List<User> interestedUsers, String message) {
    this.city = city;
    this.weatherRecommendation = weatherRecommendation;
    this.weatherAlert = weatherAlert;
    this.travelAdvisory = travelAdvisory;
    this.interestedUsers = interestedUsers;
    this.message = message;
  }

  /**
   * Default constructor.
   */
  public CitySummary() {
    this.city = "";
    this.weatherRecommendation = null;
    this.weatherAlert = null;
    this.travelAdvisory = null;
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

  public TravelAdvisory getTravelAdvisory() {
    return travelAdvisory;
  }

  public void setTravelAdvisory(TravelAdvisory travelAdvisory) {
    this.travelAdvisory = travelAdvisory;
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

  /**
   * Helper method to get country name from city using geocoding API.
   *
   * @param city the city name
   * @return the country name, or null if not found
   */
  public static String getCountryFromCity(String city) {
    try {
      String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
          + city.replace(" ", "%20") + "&count=1&language=en&format=json";
      
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(geoUrl))
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request,
          HttpResponse.BodyHandlers.ofString());

      String body = response.body();
      
      if (!body.contains("\"results\"")) {
        return null;
      }

      // Try to extract country from the response
      int countryIndex = body.indexOf("\"country\":");
      if (countryIndex != -1) {
        int start = countryIndex + 11; // Length of "country":"
        int end = body.indexOf("\"", start);
        if (end != -1) {
          return body.substring(start, end);
        }
      }

      // Alternative: try "country_code" and map to country name
      int countryCodeIndex = body.indexOf("\"country_code\":");
      if (countryCodeIndex != -1) {
        int start = countryCodeIndex + 16; // Length of "country_code":"
        int end = body.indexOf("\"", start);
        if (end != -1) {
          String countryCode = body.substring(start, end);
          // For now, return null and let the lookup try the city name as country
          // This is a simplified approach - in production you'd want a country code mapping
        }
      }

      return null;
    } catch (IOException | InterruptedException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Error geocoding city {}: {}", city, e.getMessage());
      }
      return null;
    }
  }

  @Override
  public String toString() {
    return "CitySummary {"
            + "city='" + city + '\''
            + ", weatherRecommendation=" + weatherRecommendation
            + ", weatherAlert=" + weatherAlert
            + ", travelAdvisory=" + travelAdvisory
            + ", interestedUsers=" + interestedUsers
            + ", message='" + message + '\''
            + '}';
  }
}

