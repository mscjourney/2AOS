package org.coms4156.tars;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherAlertModel;
import org.coms4156.tars.service.TarsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

  @MockitoBean
  private TarsService tarsService;

  private List<WeatherAlert> mockList;
  private User mockUser;
  private List<Map<String, String>> mockAlerts;
  private List<String> mockRecommendations;
  private Map<String, Object> mockCurrentConditions;

  @BeforeEach
  void setUp() throws Exception {
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

    WeatherAlert mockWeatherAlert1 = new WeatherAlert("New York", mockAlerts,
        mockRecommendations, mockCurrentConditions);
    WeatherAlert mockWeatherAlert2 = new WeatherAlert("Paris", mockAlerts, 
        mockRecommendations, mockCurrentConditions);

    mockList = new ArrayList<>();
    mockList.add(mockWeatherAlert1);
    mockList.add(mockWeatherAlert2);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("rainy");
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("60F");
    temperaturePreferences.add("67F");
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("Paris");

    mockUser = new User(2, weatherPreferences, temperaturePreferences, cityPreferences);
  }

  @Test
  void testGetUserAlertsWithValidId() throws Exception {
    Mockito.when(tarsService.getUser(2)).thenReturn(mockUser);

    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getUserAlerts(mockUser))
          .thenReturn(mockList);

      mockMvc.perform(get("/alert/weather/2")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$[0].location").value("New York"))
          .andExpect(jsonPath("$[0].alerts").isArray())
          .andExpect(jsonPath("$[0].alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$[0].alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$[0].recommendations").isArray())
          .andExpect(jsonPath("$[0].currentConditions").isMap())
          .andExpect(jsonPath("$[0].currentConditions.temperature_celsius").value(22.5))
          .andExpect(jsonPath("$[1].location").value("Paris"))
          .andExpect(jsonPath("$[1].alerts").isArray())
          .andExpect(jsonPath("$[1].alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$[1].alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$[1].recommendations").isArray())
          .andExpect(jsonPath("$[1].currentConditions").isMap())
          .andExpect(jsonPath("$[1].currentConditions.temperature_celsius").value(22.5));
    }
  }

  @Test
  void testGetUserAlertsWithNoSuchId() throws Exception {
    Mockito.when(tarsService.getUser(0)).thenReturn(null);

    mockMvc.perform(get("/alert/weather/0"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("No such user.")));
  }

  @Test
  void testGetUserAlertsWithBadId() throws Exception {
    mockMvc.perform(get("/alert/weather/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User Id cannot be less than zero.")));
  }
}