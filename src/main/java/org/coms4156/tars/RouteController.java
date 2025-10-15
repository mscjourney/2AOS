package org.coms4156.tars;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@RestController
public class RouteController {

  private static final String OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/forecast";
  private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search";
  private final RestTemplate restTemplate = new RestTemplate();


  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }

  @GetMapping("/alert/weather")
  public ResponseEntity<Map<String, Object>> getWeatherAlerts(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lon) {

    try {
      if (city == null && (lat == null || lon == null)) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Either 'city' or both 'lat' and 'lon' must be provided"));
      }

      double latitude;
      double longitude;
      if (lat != null && lon != null) {
        latitude = lat;
        longitude = lon;
      } else {
        Map<String, Double> coords = geocodeCity(city);
        latitude = coords.get("lat");
        longitude = coords.get("lon");
      }

      String weatherUrl = String.format(
          "%s?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m,"
          + "precipitation,rain,weather_code,wind_speed_10m&daily=weather_code,"
          + "temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max"
          + "&timezone=auto",
          OPEN_METEO_BASE_URL, latitude, longitude);

      Map<String, Object> weatherData = restTemplate.getForObject(weatherUrl, Map.class);

      List<Map<String, String>> alerts = generateAlerts(weatherData);
      List<String> recommendations = generateRecommendations(weatherData);

      Map<String, Object> response = new HashMap<>();
      response.put("location", 
          city != null ? city : String.format("%.4f, %.4f", latitude, longitude));
      response.put("timestamp", new Date().toString());
      response.put("alerts", alerts);
      response.put("recommendations", recommendations);
      response.put("current_conditions", extractCurrentConditions(weatherData));

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(Map.of("error", e.getMessage()));
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode())
          .body(Map.of("error", "Failed to fetch weather data: " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An error occurred: " + e.getMessage()));
    }
  }

  
  private Map<String, Double> geocodeCity(String city) {
    String geocodeUrl = String.format(
        "%s?name=%s&count=1&language=en&format=json",
        GEOCODING_API_URL, city);

    Map<String, Object> geocodeData = restTemplate.getForObject(geocodeUrl, Map.class);
    
    if (geocodeData == null || geocodeData.get("results") == null) {
      throw new IllegalArgumentException("City not found: " + city);
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> results = (List<Map<String, Object>>) geocodeData.get("results");

    if (results.isEmpty()) {
      throw new IllegalArgumentException("City not found: " + city);
    }

    Map<String, Object> firstResult = results.get(0);
    Map<String, Double> coords = new HashMap<>();
    coords.put("lat", ((Number) firstResult.get("latitude")).doubleValue());
    coords.put("lon", ((Number) firstResult.get("longitude")).doubleValue());
    return coords;
  }

  /**
   * Generate weather alerts based on current conditions.
   *
   * @param weatherData Weather data from Open-Meteo API
   * @return List of alerts
   */
  private List<Map<String, String>> generateAlerts(Map<String, Object> weatherData) {
    List<Map<String, String>> alerts = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    Map<String, Object> current = (Map<String, Object>) weatherData.get("current");

    if (current != null) {
      double temp = ((Number) current.get("temperature_2m")).doubleValue();
      double windSpeed = ((Number) current.get("wind_speed_10m")).doubleValue();
      double precipitation = ((Number) current.getOrDefault("precipitation", 0)).doubleValue();

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
    }

    if (alerts.isEmpty()) {
      Map<String, String> alert = new HashMap<>();
      alert.put("severity", "INFO");
      alert.put("type", "CLEAR");
      alert.put("message", "No weather alerts at this time");
      alerts.add(alert);
    }

    return alerts;
  }


  private List<String> generateRecommendations(Map<String, Object> weatherData) {
    List<String> recommendations = new ArrayList<>();
    
    @SuppressWarnings("unchecked")
    Map<String, Object> current = (Map<String, Object>) weatherData.get("current");

    if (current != null) {
      double temp = ((Number) current.get("temperature_2m")).doubleValue();
      double windSpeed = ((Number) current.get("wind_speed_10m")).doubleValue();
      double precipitation = ((Number) current.getOrDefault("precipitation", 0)).doubleValue();

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
    }

    return recommendations;
  }


  private Map<String, Object> extractCurrentConditions(Map<String, Object> weatherData) {
    Map<String, Object> conditions = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    Map<String, Object> current = (Map<String, Object>) weatherData.get("current");

    if (current != null) {
      conditions.put("temperature_celsius", current.get("temperature_2m"));
      conditions.put("humidity_percent", current.get("relative_humidity_2m"));
      conditions.put("wind_speed_kmh", current.get("wind_speed_10m"));
      conditions.put("precipitation_mm", current.getOrDefault("precipitation", 0));
      conditions.put("weather_code", current.get("weather_code"));
    }

    return conditions;
  }
}