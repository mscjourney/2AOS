package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherRecommendation;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientWeatherTest} Tests for TarsApiClient weather-related methods.
 */
public class TarsApiClientWeatherTest extends TarsApiClientTestBase {

  /**
   * {@code getWeatherRecommendationSuccessTest} Verifies successful weather recommendation.
   */
  @Test
  void getWeatherRecommendationSuccessTest() throws IOException, InterruptedException {
    WeatherRecommendation expected = new WeatherRecommendation();
    expected.setCity("New York");
    expected.setMessage("Good weather expected");
    expected.setRecommendedDays(List.of("Monday", "Tuesday"));

    testServer.createContext("/recommendation/weather/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("city=New+York"));
        assertTrue(query.contains("days=7"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    WeatherRecommendation result = client.getWeatherRecommendation("New York", 7);
    assertEquals("New York", result.getCity());
    assertEquals("Good weather expected", result.getMessage());
  }

  /**
   * {@code getWeatherRecommendationWithSpecialCharactersTest} Verifies URL encoding.
   */
  @Test
  void getWeatherRecommendationWithSpecialCharactersTest() throws IOException, InterruptedException {
    WeatherRecommendation expected = new WeatherRecommendation();
    expected.setCity("San Francisco");

    testServer.createContext("/recommendation/weather/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("city="));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    WeatherRecommendation result = client.getWeatherRecommendation("San Francisco", 5);
    assertEquals("San Francisco", result.getCity());
  }

  /**
   * {@code getWeatherAlertsByCitySuccessTest} Verifies successful weather alert retrieval by city.
   */
  @Test
  void getWeatherAlertsByCitySuccessTest() throws IOException, InterruptedException {
    WeatherAlert expected = new WeatherAlert();
    expected.setLocation("Boston");
    Map<String, String> alertMap = new HashMap<>();
    alertMap.put("severity", "moderate");
    alertMap.put("type", "rain");
    List<Map<String, String>> alerts = List.of(alertMap);
    expected.setAlerts(alerts);

    testServer.createContext("/alert/weather", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("city=Boston"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    WeatherAlert result = client.getWeatherAlertsByCity("Boston");
    assertEquals("Boston", result.getLocation());
    assertNotNull(result.getAlerts());
  }

  /**
   * {@code getWeatherAlertsByCoordinatesSuccessTest} Verifies successful alert retrieval by coordinates.
   */
  @Test
  void getWeatherAlertsByCoordinatesSuccessTest() throws IOException, InterruptedException {
    WeatherAlert expected = new WeatherAlert();
    expected.setLocation("40.7128,-74.0060");

    testServer.createContext("/alert/weather", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("lat=40.7128"));
        assertTrue(query.contains("lon=-74.006"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    WeatherAlert result = client.getWeatherAlertsByCoordinates(40.7128, -74.0060);
    assertNotNull(result);
  }

  /**
   * {@code getUserWeatherAlertsSuccessTest} Verifies successful user weather alerts retrieval.
   */
  @Test
  void getUserWeatherAlertsSuccessTest() throws IOException, InterruptedException {
    List<WeatherAlert> expectedAlerts = new ArrayList<>();
    WeatherAlert alert1 = new WeatherAlert();
    alert1.setLocation("Boston");
    WeatherAlert alert2 = new WeatherAlert();
    alert2.setLocation("New York");
    expectedAlerts.add(alert1);
    expectedAlerts.add(alert2);

    testServer.createContext("/alert/weather/user/1", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedAlerts);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    List<WeatherAlert> result = client.getUserWeatherAlerts(1);
    assertEquals(2, result.size());
    assertEquals("Boston", result.get(0).getLocation());
  }
}

