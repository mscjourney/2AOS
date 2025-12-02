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
  private List<UserPreference> interestedUsers;
  private CrimeSummary crimeSummary;
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
                     List<UserPreference> interestedUsers, String message) {
    this.city = city;
    this.weatherRecommendation = weatherRecommendation;
    this.weatherAlert = weatherAlert;
    this.travelAdvisory = travelAdvisory;
    this.interestedUsers = interestedUsers;
    this.crimeSummary = null;
    this.message = message;
  }

  /**
   *  Constructor for United States cities to not include crime data.
   */
  public CitySummary(String city, WeatherRecommendation weatherRecommendation,
                     WeatherAlert weatherAlert, TravelAdvisory travelAdvisory,
                     List<UserPreference> interestedUsers, CrimeSummary crimeSummary, 
                     String message) {
    this.city = city;
    this.weatherRecommendation = weatherRecommendation;
    this.weatherAlert = weatherAlert;
    this.travelAdvisory = travelAdvisory;
    this.interestedUsers = interestedUsers;
    this.crimeSummary = crimeSummary;
    this.message = message;
  }


  /**
   *  Default Constructor.
   */
  public CitySummary() {
    this.city = "";
    this.weatherRecommendation = null;
    this.weatherAlert = null;
    this.travelAdvisory = null;
    this.interestedUsers = null;
    this.crimeSummary = null;
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

  public List<UserPreference> getInterestedUsers() {
    return interestedUsers;
  }

  public void setInterestedUsers(List<UserPreference> interestedUsers) {
    this.interestedUsers = interestedUsers;
  }

  public CrimeSummary getCrimeSummary() {
    return crimeSummary;
  }

  public void setCrimeSummary(CrimeSummary crimeSummary) {
    this.crimeSummary = crimeSummary;
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

      // Properly returns the extraced country field from body
      // Returns null if country field could not be found
      return extractValue(body, "\"country\":\"");
    } catch (IOException | InterruptedException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Error geocoding city {}: {}", city, e.getMessage());
      }
      return null;
    }
  }

  /**
   * Retrieves the state of the US City using the open-meteo API.
   *
   * @param city the city to retrieve the state for.
   * @return returns the corresponding US state if the city is located in the US.
   *         returns null if the city is not in the United States or the state of the city
   *         (indicated by the admin1 field) cannot be found.
   */
  public static String getStateFromUsCity(String city) {
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
      String country = extractValue(body, "\"country\":\"");
      if (!"United States".equals(country)) {
        return null; // Not in the U.S.
      }

      String state = extractValue(body, "\"admin1\":\"");
      return state; // Could still be null if not present
    } catch (IOException | InterruptedException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Error geocoding city {}: {}", city, e.getMessage());
      }
      return null;
    }
  }

  /**
   *  Helper method to parse and extract specific fields from the open-meteo API.
   *
   *  @param json the json body retrieved from the open-meteo API call
   *  @param key the field of the json body to extract
   *  @return the value of the key from the json body or null if the key does not exist in the
   *          provided json body.
   */
  private static String extractValue(String json, String key) {
    int index = json.indexOf(key);
    if (index != -1) {
      int start = index + key.length();
      int end = json.indexOf("\"", start);
      if (end != -1) {
        String value = json.substring(start, end);
        if (value.contains(",")) { // ending quotation matched is next field
          return null;
        }
        return value;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "CitySummary {"
            + "city='" + city + '\''
            + ", weatherRecommendation=" + weatherRecommendation
            + ", weatherAlert=" + weatherAlert
            + ", travelAdvisory=" + travelAdvisory
            + ", interestedUsers=" + interestedUsers
            + ", crime=" + crimeSummary
            + ", message='" + message + '\''
            + '}';
  }
}

