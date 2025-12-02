package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherAlertModel;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.model.WeatherRecommendation;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.coms4156.tars.util.LoggerTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code WeatherEndpointTest} 
 * Contains Equivalence Partitions for Weather related endpoints.
 * Covers:
 *    - GET /recommendation/weather?city={city}&days={days}
 *    - GET /alert/weather?city={city}&lat={lat}&lon={lon}
 *    - GET /alert/weather/user/{userId}
 */
@SpringBootTest
@AutoConfigureMockMvc
public class WeatherEndpointTest {

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
  private UserPreference mockUser;
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
    temperaturePreferences.add("7");
    temperaturePreferences.add("15");
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("Paris");

    mockUser = new UserPreference(2L, weatherPreferences, temperaturePreferences, cityPreferences);
  }
  /* ======= /recommendation/weather Equivalence Partitions ======= */

  /**
   * {@code testGetWeatherRecommendationWithValidCityAndDays}
   * Equivalence Partition 1: city is valid and days is in the range [1, 14] (inclusive).
   * The valid cases are mocked since API will return real time values which differ everyday.
   */
  @Test
  public void testGetWeatherRecommendationWithValidCityAndDays() throws Exception {
    // Days within range
    List<String> recommendedDays = new ArrayList<>();
    recommendedDays.add("2024-01-15");
    recommendedDays.add("2024-01-16");
    WeatherRecommendation recommendation = new WeatherRecommendation(
        "New York",
        recommendedDays,
        "These days are expected to have clear weather in New York!"
    );

    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("New York", 7))
          .thenReturn(recommendation);

      mockMvc.perform(get("/recommendation/weather/")
          .param("city", "New York")
          .param("days", "7"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("New York"))
          .andExpect(jsonPath("$.recommendedDays").isArray())
          .andExpect(jsonPath("$.recommendedDays[0]").value("2024-01-15"))
          .andExpect(jsonPath("$.recommendedDays[1]").value("2024-01-16"))
          .andExpect(jsonPath("$.message")
              .value("These days are expected to have clear weather in New York!"));
    }

    // Upper Boundary of days range
    recommendedDays = new ArrayList<>();
    recommendedDays.add("2024-01-20");
    recommendation = new WeatherRecommendation(
        "Boston",
        recommendedDays,
        "These days are expected to have clear weather in Boston!"
    );

    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("Boston", 14))
          .thenReturn(recommendation);

      mockMvc.perform(get("/recommendation/weather/")
          .param("city", "Boston")
          .param("days", "14"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("Boston"))
          .andExpect(jsonPath("$.recommendedDays").isArray());
    }

    // Lower Boundary of days range
    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("Boston", 1))
          .thenReturn(recommendation);
      // RecommendedDays is at most one since we are only looking at one day range.
      mockMvc.perform(get("/recommendation/weather/")
          .param("city", "Boston")
          .param("days", "1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("Boston"))
          .andExpect(jsonPath("$.recommendedDays").isArray());
    }

    // Testing with multi-word cities
    recommendedDays = new ArrayList<>();
    recommendedDays.add("2024-01-18");
    recommendation = new WeatherRecommendation(
        "San Francisco",
        recommendedDays,
        "These days are expected to have clear weather in San Francisco!"
    );

    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("San Francisco", 5))
          .thenReturn(recommendation);

      mockMvc.perform(get("/recommendation/weather/")
          .param("city", "San Francisco")
          .param("days", "5")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("San Francisco"))
          .andExpect(jsonPath("$.recommendedDays").isArray());
    }
  }

  /**
   * {@code}
   * Equivalence Partition 2: An invalid city is passed in with valid days range.
   * Will return OK but will contain an error message instead of proper data.
   */
  @Test
  public void testGetWeatherRecommendationInvalidCityValidDays() throws Exception {
    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "109123")
        .param("days", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", containsString("Error processing forecast")));

    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "asdasdsad")
        .param("days", "14"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", containsString("Error processing forecast")));

    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "!!!")
        .param("days", "7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", containsString("Error processing forecast")));
  }
  
  /**
   * {@code testGetWeatherRecommendationWithInvalidDays}
   * Equivalence Partition 3: days is not in the specified range.
   * In this case, we do not care about the validity of the city because entry point will return
   * BAD_REQUEST regardless of the validity of city.
   */

  @Test
  public void testGetWeatherRecommendationWithInvalidDays() throws Exception {
    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "0"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "15"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "-5"))
        .andExpect(status().isBadRequest());
  }

  /* ======= /alert/weather?city={city}&lat={lat}&lon={lon} Equivalence Partitions ======= */
  
  /** 
   * {@code testGetWeatherAlertsWithValidCoordinates}
   * Equivalence Partition 1: Lat and Lon are both passed in.
   * Lat and Lon take precedence over city param. Regardless of city being passed in or not, the
   * lat and lon values will be used to generate the alert.
   */
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

    // Unusual coordinates are OK
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

  
  /**
   * {@code testGetWeatherAlertCityOnly}
   * Equivalence Partition 2: city is passed in. Either lat or lon is not passed OR neither
   * are passed in.
   * Lat and lon BOTH must be passed for it to be used over the city.
   */
  @Test
  public void testGetWeatherAlertCityOnly() throws Exception {
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "New York", null, null))
          .thenReturn(mockWeatherAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "New York"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.location").value("New York"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$.alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap())
          .andExpect(jsonPath("$.currentConditions.temperature_celsius")
              .value(22.5));
    }

    // City and Lat passed in -> use city to generate
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "New York", 35.229, null))
          .thenReturn(mockWeatherAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "New York")
          .param("lat", "35.229"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.location").value("New York"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$.alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap())
          .andExpect(jsonPath("$.currentConditions.temperature_celsius")
              .value(22.5));
    }

    // City and Lon passed in -> use city to generate
    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getWeatherAlerts(
          "New York", null, -48.1943))
          .thenReturn(mockWeatherAlert);

      mockMvc.perform(get("/alert/weather")
          .param("city", "New York")
          .param("lon", "-48.1943"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.location").value("New York"))
          .andExpect(jsonPath("$.alerts").isArray())
          .andExpect(jsonPath("$.alerts[0].severity").value("INFO"))
          .andExpect(jsonPath("$.alerts[0].type").value("CLEAR"))
          .andExpect(jsonPath("$.recommendations").isArray())
          .andExpect(jsonPath("$.currentConditions").isMap())
          .andExpect(jsonPath("$.currentConditions.temperature_celsius")
              .value(22.5));
    }

    // multi-word and/or accented cities are fine
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

  /**
   * {@code testGetWeatherAlertInvalidCity}
   * Equivalence Partition 3: invalid city is passed in. Either lat or lon is not passed OR neither
   * are passed in. Invalid city take precedence.
   */
  @Test
  public void testGetWeatherAlertInvalidCity() throws Exception {
    mockMvc.perform(get("/alert/weather")
        .param("city", "asdoij"))
          .andExpect(status().isBadRequest());
      
    mockMvc.perform(get("/alert/weather")
        .param("city", "!!!")
        .param("lat", "39.213"))
          .andExpect(status().isBadRequest());
      
    mockMvc.perform(get("/alert/weather")
        .param("city", "!!!")
        .param("lon", "-19.213"))
          .andExpect(status().isBadRequest());
  }

  /**
   * {@code testGetWeatherAlertNoParam}
   * Equivalence Partition 4: city, lat, lon are ALL NOT passed in OR just lat or just lon is 
   * passed in. If no city and only lat or lon (not both) are passed in, also returns a BAD_REQUEST.
   */
  @Test
  public void testGetWeatherAlertNoParam() throws Exception {
    mockMvc.perform(get("/alert/weather"))
        .andExpect(status().isBadRequest());

    mockMvc.perform(get("/alert/weather")
      .param("lat", "42.1345"))
        .andExpect(status().isBadRequest());
    
    mockMvc.perform(get("/alert/weather")
      .param("lon", "-32.15"))
        .andExpect(status().isBadRequest());
  }

  /* ======= /alert/weather/user/{userId} Equivalence Partitions ======== */

  /**
   * {@code testGetUserAlertsWithValidId}
   * Equivalence Partition 1: id is non-negative, there is a TarsUser 
   *    associated with that id, and userPreference for that TarsUser has been set.
   */
  @Test
  public void testGetUserAlertsWithValidId() throws Exception {
    TarsUser tarsUser = new TarsUser(2L, "Denise", "den@gmail.com", "user");
    Mockito.when(tarsUserService.findById(2L)).thenReturn(tarsUser);
    Mockito.when(tarsService.getUserPreference(2L)).thenReturn(mockUser);

    try (MockedStatic<WeatherAlertModel> mockedModel =
        Mockito.mockStatic(WeatherAlertModel.class)) {
      mockedModel.when(() -> WeatherAlertModel.getUserAlerts(mockUser))
          .thenReturn(mockList);

      mockMvc.perform(get("/alert/weather/user/2"))
          .andExpect(status().isOk())
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
  
  /**
   * {@code testGetUserWeatherAlertsNoPreferences}
   * Equivalence Partition 2: id is non-negative, there is a TarsUser 
   *    associated with that id, but no userPreference has been previously set.
   */
  @Test
  public void testGetUserWeatherAlertsNoPreferences() throws Exception {
    Mockito.when(tarsService.getUserPreference(4L)).thenReturn(null);
    
    TarsUser tarsUser = new TarsUser(4L, "Denise", "den@gmail.com", "user");
    Mockito.when(tarsUserService.findById(4L)).thenReturn(tarsUser);

    mockMvc.perform(get("/alert/weather/user/4"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User Preferences not found."));
  }

  /**
   * {@code testGetUserWeatherAlertsNoTarsUser}
   * Equivalence Partition 3: id is non-negative, but there is no TarsUser
   *    associated with the id.
   */
  @Test
  public void testGetUserWeatherAlertsNoTarsUser() throws Exception {
    mockMvc.perform(get("/alert/weather/user/0"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));
    
    mockMvc.perform(get("/alert/weather/user/12308"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));
  }

  /**
   * {@code testGetUserWeatherAlertsNegativeId} 
   * Equivalence Partition 4: id is negative.
   */
  @Test
  public void testGetUserWeatherAlertsNegativeId() throws Exception {
    mockMvc.perform(get("/alert/weather/user/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be less than zero."));
    
    mockMvc.perform(get("/alert/weather/user/-1290"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be less than zero."));
  }

  /* ======= Logging Branch Coverage Tests for Weather related Endpoints ======= */
  
  /**
   * {@code getWeatherRecommendationInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getWeatherRecommendationInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      mockMvc.perform(get("/recommendation/weather/")
        .param("city", "New York")
        .param("days", "13"))
          .andExpect(status().isOk());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code getWeatherAlertsInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getWeatherAlertsInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {
              
      mockMvc.perform(get("/alert/weather")
        .param("city", "Los Angeles"))
          .andExpect(status().isOk());
      
      assertFalse(cap.hasLevel(Level.INFO), "INFO suppressed at WARN.");
    }
  }
  
  /**
   * {@code getWeatherAlertsInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void getUserWeatherAlertsInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {

      TarsUser tarsUser = new TarsUser(2L, "Denise", "den@gmail.com", "user");
      Mockito.when(tarsUserService.findById(2L)).thenReturn(tarsUser);
      Mockito.when(tarsService.getUserPreference(2L)).thenReturn(mockUser);

      try (MockedStatic<WeatherAlertModel> mockedModel =
          Mockito.mockStatic(WeatherAlertModel.class)) {
        mockedModel.when(() -> WeatherAlertModel.getUserAlerts(mockUser))
            .thenReturn(mockList);    
          
        mockMvc.perform(get("/alert/weather/user/2")
          .param("city", "Los Angeles"))
            .andExpect(status().isOk());
        
        assertFalse(
            cap.hasLevel(Level.INFO),
            "INFO suppressed at WARN."
        );
      }
    }
  }

  /**
   * {@code getWeatherRecommendationWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getWeatherRecommendationWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      
      mockMvc.perform(get("/recommendation/weather/")
        .param("city", "New York")
        .param("days", "20"))
          .andExpect(status().isBadRequest());
      
      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code getWeatherAlertWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getWeatherAlertWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      
      mockMvc.perform(get("/alert/weather")
        .param("lat", "20.9104"))
          .andExpect(status().isBadRequest());
      
      mockMvc.perform(get("/alert/weather")
        .param("city", "!!!"))
          .andExpect(status().isBadRequest());

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }
  
  /**
   * {@code getUserWeatherAlertWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void getUserWeatherAlertWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      
      mockMvc.perform(get("/alert/weather/user/-1"))
          .andExpect(status().isBadRequest());
      
      mockMvc.perform(get("/alert/weather/user/1"))
          .andExpect(status().isNotFound());

      TarsUser tarsUser = new TarsUser(2L, "Denise", "den@gmail.com", "user");
      Mockito.when(tarsUserService.findById(2L)).thenReturn(tarsUser);

      mockMvc.perform(get("/alert/weather/user/2"))
          .andExpect(status().isNotFound());

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }
}

