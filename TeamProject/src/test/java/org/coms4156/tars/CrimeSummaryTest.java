package org.coms4156.tars;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.controller.RouteController;
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


@WebMvcTest(RouteController.class)
public class CrimeModelTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  private WeatherAlert mockWeatherAlert;
  private List<Map<String, String>> mockAlerts;
  private List<String> mockRecommendations;
  private Map<String, Object> mockCurrentConditions;

  @BeforeEach
  void setUp() {

  }


  @Test
  void testGetWeatherAlertsWithValidCity() throws Exception {

  }

}