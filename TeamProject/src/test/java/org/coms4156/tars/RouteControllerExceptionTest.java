package org.coms4156.tars;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code RouteControllerExceptionTest} Tests exception handling
 * in RouteController endpoints.
 */
@WebMvcTest(RouteController.class)
public class RouteControllerExceptionTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  @MockitoBean
  private ClientService clientService;
  @MockitoBean
  private TarsUserService tarsUserService;

  @Test
  public void testGetUserListExceptionHandling() throws Exception {
    Mockito.when(tarsService.getUserPreferenceList())
        .thenThrow(new RuntimeException("Database error"));

    mockMvc.perform(get("/userPreferenceList"))
        .andExpect(status().isInternalServerError());
  }


  // @Test
  // public void getClientUserListException() throws Exception {
  //   Mockito.when(tarsService.getUserPreferenceList())
  //       .thenThrow(new RuntimeException("Database error"));

  //   mockMvc.perform(get("/userList/client/123"))
  //       .andExpect(status().isInternalServerError());
  // }
  
  @Test
  public void setUserPreferenceNullBody() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    
    List<String> emptyPreference = new ArrayList<>();
    UserPreference preference = 
        new UserPreference(1L, emptyPreference, emptyPreference, emptyPreference);
    // Return some dummy TarsUser
    Mockito.when(tarsUserService.findById(1L)).thenReturn(new TarsUser());
    // Simulate that null was passed into setUserPreference
    Mockito.when(tarsService.setUserPreference(preference)).thenReturn(false);

    mockMvc.perform(put("/setPreference/1")
      .contentType("application/json")
      .content(mapper.writeValueAsString(preference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid Argument. User Body was null."));
  }

  @Test
  public void testGetWeatherRecommendationExceptionHandling() throws Exception {
    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getRecommendedDays("InvalidCity", 7))
          .thenThrow(new RuntimeException("API error"));

      mockMvc.perform(get("/recommendation/weather")
          .param("city", "InvalidCity")
          .param("days", "7")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(content().string("API Failure."));
    }
  }

  @Test
  public void testGetUserWeatherRecommendationExceptionHandling() throws Exception {
    try (MockedStatic<WeatherModel> mockedModel = Mockito.mockStatic(WeatherModel.class)) {
      mockedModel.when(() -> WeatherModel.getUserRecDays("InvalidCity", 7, new UserPreference()))
          .thenThrow(new RuntimeException("API error"));

      when(tarsUserService.findById(2L)).thenReturn(new TarsUser());
      when(tarsService.getUserPreference(2L)).thenReturn(new UserPreference());
      
      mockMvc.perform(get("/recommendation/weather/user/2")
          .param("city", "InvalidCity")
          .param("days", "7")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError())
          .andExpect(content().string("API Failure."));
    }
  }

  @Test
  public void testGetWeatherAlertsIllegalArgumentException() throws Exception {
    org.coms4156.tars.model.WeatherAlertModel mockModel = 
        Mockito.mock(org.coms4156.tars.model.WeatherAlertModel.class);
    
    try (MockedStatic<org.coms4156.tars.model.WeatherAlertModel> mockedModel = 
        Mockito.mockStatic(org.coms4156.tars.model.WeatherAlertModel.class)) {
      mockedModel.when(() -> org.coms4156.tars.model.WeatherAlertModel.getWeatherAlerts(
          "InvalidCity", null, null))
          .thenThrow(new IllegalArgumentException("Invalid city"));

      mockMvc.perform(get("/alert/weather")
          .param("city", "InvalidCity")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  public void testGetWeatherAlertsGeneralException() throws Exception {
    try (MockedStatic<org.coms4156.tars.model.WeatherAlertModel> mockedModel = 
        Mockito.mockStatic(org.coms4156.tars.model.WeatherAlertModel.class)) {
      mockedModel.when(() -> org.coms4156.tars.model.WeatherAlertModel.getWeatherAlerts(
          "ErrorCity", null, null))
          .thenThrow(new RuntimeException("Network error"));

      mockMvc.perform(get("/alert/weather")
          .param("city", "ErrorCity")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());
    }
  }

  @Test
  public void testGetUserWeatherAlertsIllegalArgumentException() throws Exception {
    org.coms4156.tars.model.UserPreference mockUser = new org.coms4156.tars.model.UserPreference(1L,
        java.util.List.of("sunny"), java.util.List.of("13"), java.util.List.of("Boston"));
    Mockito.when(tarsService.getUserPreference(1L)).thenReturn(mockUser);

    org.coms4156.tars.model.TarsUser tarsUser = new TarsUser(1L, "Denise", "den@gmail.com", "user");
    Mockito.when(tarsUserService.findById(1L)).thenReturn(tarsUser);
    
    try (MockedStatic<org.coms4156.tars.model.WeatherAlertModel> mockedModel = 
        Mockito.mockStatic(org.coms4156.tars.model.WeatherAlertModel.class)) {
      mockedModel.when(() -> org.coms4156.tars.model.WeatherAlertModel.getUserAlerts(mockUser))
          .thenThrow(new IllegalArgumentException("Invalid user data"));

      mockMvc.perform(get("/alert/weather/user/1")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  public void testGetUserWeatherAlertsGeneralException() throws Exception {
    org.coms4156.tars.model.UserPreference mockUser = new org.coms4156.tars.model.UserPreference(1L,
        java.util.List.of("sunny"), java.util.List.of("13"), java.util.List.of("Boston"));
    
    org.coms4156.tars.model.TarsUser tarsUser = new TarsUser(1L, "Denise", "den@gmail.com", "user");

    Mockito.when(tarsUserService.findById(1L)).thenReturn(tarsUser);
    Mockito.when(tarsService.getUserPreference(1L)).thenReturn(mockUser);

    try (MockedStatic<org.coms4156.tars.model.WeatherAlertModel> mockedModel = 
        Mockito.mockStatic(org.coms4156.tars.model.WeatherAlertModel.class)) {
      mockedModel.when(() -> org.coms4156.tars.model.WeatherAlertModel.getUserAlerts(mockUser))
          .thenThrow(new RuntimeException("Unexpected error"));

      mockMvc.perform(get("/alert/weather/user/1")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());
    }
  }

  @Test
  public void testGetCrimeSummaryExceptionHandling() throws Exception {
    try (org.mockito.MockedConstruction<org.coms4156.tars.model.CrimeModel> mocked = 
        org.mockito.Mockito.mockConstruction(
            org.coms4156.tars.model.CrimeModel.class,
            (mock, context) -> {
              Mockito.when(mock.getCrimeSummary("NC", "V", "10", "2025"))
                  .thenThrow(new RuntimeException("API error"));
            })) {

      mockMvc.perform(get("/crime/summary")
          .param("state", "NC")
          .param("offense", "V")
          .param("month", "10")
          .param("year", "2025")
          .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isInternalServerError());
    }
  }

  @Test
  public void testGetCountryAdvisoryIllegalArgumentException() throws Exception {
    try (org.mockito.MockedConstruction<org.coms4156.tars.model.TravelAdvisoryModel> mocked = 
        org.mockito.Mockito.mockConstruction(
            org.coms4156.tars.model.TravelAdvisoryModel.class,
            (mock, context) -> {
              Mockito.when(mock.getTravelAdvisory("Canada"))
                  .thenThrow(new IllegalArgumentException("Invalid Country Error"));
            })) {

      mockMvc.perform(get("/country/Canada"))
        .andExpect(status().isBadRequest())
          .andExpect(content().string("Invalid Country Error"));
    }
  }

  @Test
  public void testGetCountryAdvisoryGeneralException() throws Exception {
    try (org.mockito.MockedConstruction<org.coms4156.tars.model.TravelAdvisoryModel> mocked = 
        org.mockito.Mockito.mockConstruction(
            org.coms4156.tars.model.TravelAdvisoryModel.class,
            (mock, context) -> {
              Mockito.when(mock.getTravelAdvisory("Canada"))
                  .thenThrow(new RuntimeException("Invalid Country Error"));
            })) {

      mockMvc.perform(get("/country/Canada"))
          .andExpect(status().isInternalServerError());
    }
  }
}

