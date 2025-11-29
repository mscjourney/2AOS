package org.coms4156.tars;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.model.WeatherRecommendation;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code WeatherRecommendationTest} Unit tests for 
 * weather recommendation endpoint in RouteController.
 */
@WebMvcTest(RouteController.class)
public class WeatherRecommendationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  @MockitoBean
  private ClientService clientService;

  @MockitoBean
  private TarsUserService tarsUserService;


  @Test
  public void testGetWeatherRecommendationWithValidCityAndDays() throws Exception {
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
          .param("days", "7")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("New York"))
          .andExpect(jsonPath("$.recommendedDays").isArray())
          .andExpect(jsonPath("$.recommendedDays[0]").value("2024-01-15"))
          .andExpect(jsonPath("$.recommendedDays[1]").value("2024-01-16"))
          .andExpect(jsonPath("$.message")
              .value("These days are expected to have clear weather in New York!"));
    }
  }

  @Test
  public void testGetWeatherRecommendationWithInvalidDaysZero() throws Exception {
    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "0")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetWeatherRecommendationWithInvalidDaysTooLarge() throws Exception {
    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "15")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetWeatherRecommendationWithValidMaxDays() throws Exception {
    List<String> recommendedDays = new ArrayList<>();
    recommendedDays.add("2024-01-20");
    WeatherRecommendation recommendation = new WeatherRecommendation(
        "Boston",
        recommendedDays,
        "These days are expected to have clear weather in Boston!"
    );

    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("Boston", 14))
          .thenReturn(recommendation);

      mockMvc.perform(get("/recommendation/weather/")
          .param("city", "Boston")
          .param("days", "14")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.city").value("Boston"))
          .andExpect(jsonPath("$.recommendedDays").isArray());
    }
  }

  @Test
  public void testGetWeatherRecommendationWithNegativeDays() throws Exception {
    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "Boston")
        .param("days", "-1")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetWeatherRecommendationWithCityContainingSpaces() throws Exception {
    List<String> recommendedDays = new ArrayList<>();
    recommendedDays.add("2024-01-18");
    WeatherRecommendation recommendation = new WeatherRecommendation(
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
}

