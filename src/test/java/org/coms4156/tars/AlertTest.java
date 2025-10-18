package org.coms4156.tars;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.WeatherAlert;
import model.WeatherAlertModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprehensive test suite for the /alert/weather endpoint.
 * This test class includes:
 * - Tests for typical valid inputs (city name, lat/lon coordinates)
 * - Tests for atypical valid inputs (edge cases, special characters)
 * - Tests for invalid inputs (missing parameters, invalid coordinates)
 */
@WebMvcTest(RouteController.class)
public class AlertTest {

  @Autowired
  private MockMvc mockMvc;

  private WeatherAlert mockWeatherAlert;
  private List<Map<String, String>> mockAlerts;
  private List<String> mockRecommendations;
  private Map<String, Object> mockCurrentConditions;

  @BeforeEach
  void setUp() {
    // Setup mock data for testing
    mockAlerts = new ArrayList<>();
    Map<String, String> alert = new HashMap<>();
    alert.put("severity", "INFO");
    alert.put("type", "CLEAR");
    alert.put("message", "No weather alerts at this time");
    mockAlerts.add(alert);

    mockRecommendations = new ArrayList<>();
    mockRecommendations.add("Great weather for outdoor activities");

    mockCurrentConditions = new HashMap<>();
    mockCurrentConditions.put("temperature_celsius", 22.5);
    mockCurrentConditions.put("humidity_percent", 65.0);
    mockCurrentConditions.put("wind_speed_kmh", 15.0);
    mockCurrentConditions.put("precipitation_mm", 0.0);
    mockCurrentConditions.put("weather_code", 1);

    mockWeatherAlert = new WeatherAlert("New York", mockAlerts,
        mockRecommendations, mockCurrentConditions);
  }


  @Test
  void testGetWeatherAlertsWithValidCity() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "New York", null, null)).thenReturn(mockWeatherAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "New York")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("New York"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$.alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap())
          .andExpect(jsonPath("$.currentConditions.temperature_celsius")
              .value(22.5));
    }
  }


  @Test
  void testGetWeatherAlertsWithValidCoordinates() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert coordAlert = new WeatherAlert("40.7128, -74.0060",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          null, 40.7128, -74.0060)).thenReturn(coordAlert);

      mockMvc.perform(get("/alert/weather")
          .param("lat", "40.7128")
          .param("lon", "-74.0060")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("40.7128, -74.0060"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap());
    }
  }


  @Test
  void testGetWeatherAlertsWithCityContainingSpaces() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert spacedAlert = new WeatherAlert("San Francisco",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "San Francisco", null, null)).thenReturn(spacedAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "San Francisco")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("San Francisco"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap());
    }
  }


  /**
   * Test: city name with accented characters.
   */
  @Test
  void testGetWeatherAlertsWithAccentedCityName() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert accentedAlert = new WeatherAlert("São Paulo",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "São Paulo", null, null)).thenReturn(accentedAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "São Paulo")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("São Paulo"));
    }
  }

  /**
   * Test: Edge case - coordinates at extreme values.
   * Tests coordinates at the boundaries of valid latitude/longitude ranges.
   */
  @Test
  void testGetWeatherAlertsWithExtremeCoordinates() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert extremeAlert = new WeatherAlert("90.0000, 180.0000",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          null, 90.0, 180.0)).thenReturn(extremeAlert);

      mockMvc.perform(get("/alert/weather")
          .param("lat", "90.0")
          .param("lon", "180.0")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("90.0000, 180.0000"));
    }
  }

  /**
   * Test: Invalid input - no parameters provided.
   * Tests the validation when neither city nor coordinates are provided.
   */
  @Test
  void testGetWeatherAlertsWithNoParameters() throws Exception {
    mockMvc.perform(get("/alert/weather")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  /**
   * Test: Invalid input - only latitude provided.
   * Tests validation when only one coordinate is provided.
   */
  @Test
  void testGetWeatherAlertsWithOnlyLatitude() throws Exception {
    mockMvc.perform(get("/alert/weather")
        .param("lat", "40.7128")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

}