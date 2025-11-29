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
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
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
 * {@code AlertTest} Unit tests for weather alert endpoints in RouteController.
 */
@WebMvcTest(RouteController.class)
public class AlertTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  @MockitoBean
  private ClientService clientService;

  @MockitoBean
  private TarsUserService tarsUserService;


  private List<WeatherAlert> mockList;
  private WeatherAlert mockWeatherAlert;
  private User mockUser;
  private List<Map<String, String>> mockAlerts;
  private List<String> mockRecommendations;
  private Map<String, Object> mockCurrentConditions;

  @BeforeEach
  void setUp() throws Exception {
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
    WeatherAlert mockWeatherAlert2 = new WeatherAlert("Paris", mockAlerts, 
        mockRecommendations, mockCurrentConditions);

    mockList = new ArrayList<>();
    mockList.add(mockWeatherAlert);
    mockList.add(mockWeatherAlert2);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("rainy");
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("60F");
    temperaturePreferences.add("67F");
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("Paris");

    mockUser = new User(2, 2, weatherPreferences, temperaturePreferences, cityPreferences);
  }

  @Test
  void testGetUserAlertsWithValidId() throws Exception {
    Mockito.when(tarsService.getUser(2)).thenReturn(mockUser);

    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getUserAlerts(mockUser))
          .thenReturn(mockList);

      mockMvc.perform(get("/alert/weather/user/2")
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

    mockMvc.perform(get("/alert/weather/user/0"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("No such user.")));
  }

  @Test
  void testGetUserAlertsWithBadId() throws Exception {
    mockMvc.perform(get("/alert/weather/user/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("User Id cannot be less than zero.")));
  }

  @Test
  void testGetUserAlertsWithValidIdButEmptyAlerts() throws Exception {
    Mockito.when(tarsService.getUser(3)).thenReturn(mockUser);

    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      List<WeatherAlert> emptyList = new ArrayList<>();
      mockedModel.when(() -> WeatherAlertModel.getUserAlerts(mockUser))
          .thenReturn(emptyList);

      mockMvc.perform(get("/alert/weather/user/3")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$").isEmpty());
    }
  }

  @Test
  void testGetWeatherAlertsWithValidCity() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "New York", null, null))
          .thenReturn(mockWeatherAlert);

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
          null, 40.7128, -74.0060))
          .thenReturn(coordAlert);

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
          "San Francisco", null, null))
          .thenReturn(spacedAlert);

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


  @Test
  void testGetWeatherAlertsWithAccentedCityName() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert accentedAlert = new WeatherAlert("São Paulo",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "São Paulo", null, null))
          .thenReturn(accentedAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "São Paulo")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("São Paulo"));
    }
  }


  @Test
  void testGetWeatherAlertsWithExtremeCoordinates() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      WeatherAlert extremeAlert = new WeatherAlert("90.0000, 180.0000",
          mockAlerts, mockRecommendations, mockCurrentConditions);
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          null, 90.0, 180.0))
          .thenReturn(extremeAlert);

      mockMvc.perform(get("/alert/weather")
          .param("lat", "90.0")
          .param("lon", "180.0")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.location").value("90.0000, 180.0000"));
    }
  }


  @Test
  void testGetWeatherAlertsWithNoParameters() throws Exception {
    mockMvc.perform(get("/alert/weather")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }


  @Test
  void testGetWeatherAlertsWithOnlyLatitude() throws Exception {
    mockMvc.perform(get("/alert/weather")
        .param("lat", "40.7128")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}