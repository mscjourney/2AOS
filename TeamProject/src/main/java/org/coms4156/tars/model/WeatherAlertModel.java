package org.coms4156.tars.model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles weather alert data retrieval and processing.
 */
public class WeatherAlertModel {

  private static final HttpClient client = HttpClient.newHttpClient();
  private static final String OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast";
  private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search";

  /**
   * Geocodes a city name to latitude and longitude coordinates.
   *
   * @param city the name of the city to geocode
   * @return a Map containing "lat" and "lon" keys with their values
   * @throws IllegalArgumentException if the city cannot be found
   */
  private static Map<String, Double> geocodeCity(String city) {
    try {
      String geocodeUrl = GEOCODING_API_URL + "?name=" 
          + city.replace(" ", "%20") + "&count=1&language=en&format=json";
      
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(geocodeUrl))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      String body = response.body();
      
      if (!body.contains("\"results\"")) {
        throw new IllegalArgumentException("City not found: " + city);
      }

      int latIndex = body.indexOf("\"latitude\":");
      int lonIndex = body.indexOf("\"longitude\":");
      
      if (latIndex == -1 || lonIndex == -1) {
        throw new IllegalArgumentException("City not found: " + city);
      }

      double latitude = Double.parseDouble(body.substring(latIndex + 11,
          body.indexOf(",", latIndex)).trim());
      double longitude = Double.parseDouble(body.substring(lonIndex + 12,
          body.indexOf(",", lonIndex)).trim());

      Map<String, Double> coords = new HashMap<>();
      coords.put("lat", latitude);
      coords.put("lon", longitude);
      return coords;

    } catch (IOException | InterruptedException e) {
      throw new IllegalArgumentException("Error geocoding city: " + e.getMessage());
    }
  }

  /**
   * Fetches weather data from the Open-Meteo API.
   *
   * @param latitude the latitude coordinate
   * @param longitude the longitude coordinate
   * @return the raw JSON response as a String
   */
  private static String fetchWeatherData(double latitude, double longitude) {
    try {
      String weatherUrl = String.format(
          "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,"
          + "precipitation,rain,weather_code,wind_speed_10m&daily=weather_code,"
          + "temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max"
          + "&timezone=auto",
          OPEN_METEO_BASE_URL, latitude, longitude);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(weatherUrl))
          .GET()
          .build();

      HttpResponse<String> response = client.send(request,
          HttpResponse.BodyHandlers.ofString());

      return response.body();

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Error fetching weather data: " + e.getMessage());
    }
  }

  /**
   * Generates weather alerts based on current conditions.
   *
   * @param weatherJson the raw JSON weather data
   * @return a list of alert maps containing severity, type, and message
   */
  private static List<Map<String, String>> generateAlerts(String weatherJson) {
    List<Map<String, String>> alerts = new ArrayList<>();

    try {
      // Parse current weather data
      String currentSection = weatherJson.split("\"current\":")[1].split("}")[0] + "}";
      
      double temp = parseValue(currentSection, "temperature_2m");
      double windSpeed = parseValue(currentSection, "wind_speed_10m");
      double precipitation = parseValue(currentSection, "precipitation");

      // Temperature alerts
      if (temp > 35) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", "HIGH");
        alert.put("type", "HEAT");
        alert.put("message", "Extreme heat advisory: Temperature above 35°C");
        alerts.add(alert);
      } else if (temp < 0) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", "MEDIUM");
        alert.put("type", "COLD");
        alert.put("message", "Freezing conditions: Temperature below 0°C");
        alerts.add(alert);
      }

      // Wind alerts
      if (windSpeed > 50) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", "HIGH");
        alert.put("type", "WIND");
        alert.put("message", "High wind warning: Wind speeds above 50 km/h");
        alerts.add(alert);
      }

      // Precipitation alerts
      if (precipitation > 5) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", "MEDIUM");
        alert.put("type", "RAIN");
        alert.put("message", "Heavy precipitation: Rain exceeding 5mm");
        alerts.add(alert);
      }

      if (alerts.isEmpty()) {
        Map<String, String> alert = new HashMap<>();
        alert.put("severity", "INFO");
        alert.put("type", "CLEAR");
        alert.put("message", "No weather alerts at this time");
        alerts.add(alert);
      }

    } catch (Exception e) {
      Map<String, String> alert = new HashMap<>();
      alert.put("severity", "ERROR");
      alert.put("type", "SYSTEM");
      alert.put("message", "Error processing weather data");
      alerts.add(alert);
    }

    return alerts;
  }

  /**
   * Generates recommendations based on current weather conditions.
   *
   * @param weatherJson the raw JSON weather data
   * @return a list of recommendation strings
   */
  private static List<String> generateRecommendations(String weatherJson) {
    List<String> recommendations = new ArrayList<>();

    try {
      String currentSection = weatherJson.split("\"current\":")[1].split("}")[0] + "}";
      
      double temp = parseValue(currentSection, "temperature_2m");
      double windSpeed = parseValue(currentSection, "wind_speed_10m");
      double precipitation = parseValue(currentSection, "precipitation");

      if (precipitation > 2) {
        recommendations.add("Bring an umbrella or raincoat");
        recommendations.add("Plan indoor activities");
      } else if (temp > 15 && temp < 28 && windSpeed < 20) {
        recommendations.add("Great weather for outdoor activities");
        recommendations.add("Good day for sightseeing");
      }

      if (temp > 30) {
        recommendations.add("Stay hydrated and use sun protection");
      } else if (temp < 5) {
        recommendations.add("Dress warmly in layers");
      }

      if (windSpeed > 30) {
        recommendations.add("Secure loose items and avoid exposed areas");
      }

    } catch (Exception e) {
      recommendations.add("Unable to generate recommendations at this time");
    }

    return recommendations;
  }

  /**
   * Extracts current weather conditions from the JSON response.
   *
   * @param weatherJson the raw JSON weather data
   * @return a map of current condition keys and values
   */
  private static Map<String, Object> extractCurrentConditions(String weatherJson) {
    Map<String, Object> conditions = new HashMap<>();

    try {
      String currentSection = weatherJson.split("\"current\":")[1].split("}")[0] + "}";
      
      conditions.put("temperature_celsius", parseValue(currentSection, "temperature_2m"));
      conditions.put("humidity_percent", parseValue(currentSection, "relative_humidity_2m"));
      conditions.put("wind_speed_kmh", parseValue(currentSection, "wind_speed_10m"));
      conditions.put("precipitation_mm", parseValue(currentSection, "precipitation"));
      conditions.put("weather_code", (int) parseValue(currentSection, "weather_code"));

    } catch (Exception e) {
      conditions.put("error", "Unable to parse current conditions");
    }

    return conditions;
  }

  /**
   * Helper method to parse a numeric value from JSON string.
   *
   * @param json the JSON string section
   * @param key the key to extract
   * @return the numeric value, or 0 if not found
   */
  private static double parseValue(String json, String key) {
    try {
      int index = json.indexOf("\"" + key + "\":");
      if (index == -1) {
        return 0;
      }
      String substring = json.substring(index + key.length() + 3);
      int endIndex = substring.indexOf(",");
      if (endIndex == -1) {
        endIndex = substring.indexOf("}");
      }
      return Double.parseDouble(substring.substring(0, endIndex).trim());
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Gets weather alerts and recommendations for a given location.
   *
   * @param city the city name (can be null if lat/lon provided)
   * @param lat the latitude (can be null if city provided)
   * @param lon the longitude (can be null if city provided)
   * @return a WeatherAlert object containing all alert information
   * @throws IllegalArgumentException if location parameters are invalid
   */
  public static WeatherAlert getWeatherAlerts(String city, Double lat, Double lon) {
    double latitude;
    double longitude;
    String locationName;

    if (lat != null && lon != null) {
      latitude = lat;
      longitude = lon;
      locationName = String.format("%.4f, %.4f", latitude, longitude);
    } else if (city != null) {
      Map<String, Double> coords = geocodeCity(city);
      latitude = coords.get("lat");
      longitude = coords.get("lon");
      locationName = city;
    } else {
      throw new IllegalArgumentException(
          "Either 'city' or both 'lat' and 'lon' must be provided");
    }

    String weatherData = fetchWeatherData(latitude, longitude);
    
    List<Map<String, String>> alerts = generateAlerts(weatherData);
    List<String> recommendations = generateRecommendations(weatherData);
    Map<String, Object> currentConditions = extractCurrentConditions(weatherData);

    return new WeatherAlert(locationName, alerts, recommendations, currentConditions);
  }
}