package org.coms4156.tars;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for the /crime/summary endpoint.
 * This suite verifies:
 *  - Successful responses for valid parameters
 *  - Proper JSON structure in the response
 *  - Handling of invalid or missing query parameters
 */
@WebMvcTest(RouteController.class)
public class CrimeEndpointTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  @MockitoBean
  private ClientService clientService;

  @MockitoBean
  private TarsUserService tarsUserService;

  /**
   * Test a valid GET request for a state crime summary.
   */
  @Test
  void testGetCrimeSummaryWithValidParams() throws Exception {
    try (MockedConstruction<CrimeModel> mocked = Mockito.mockConstruction(
            CrimeModel.class,
            (mock, context) -> when(mock.getCrimeSummary("NC", "V", "10", "2025"))
                    .thenReturn("9.57 cases per 100,000 people"))) {

      mockMvc.perform(get("/crime/summary")
                      .param("state", "NC")
                      .param("offense", "V")
                      .param("month", "10")
                      .param("year", "2025"))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.state").value("NC"))
              .andExpect(jsonPath("$.month").value("10"))
              .andExpect(jsonPath("$.year").value("2025"))
              .andExpect(jsonPath("$.message")
                      .value(containsString("9.57 cases per 100,000 people")));
    }
  }

  /**
   * Test missing parameters.
   */
  @Test
  void testGetCrimeSummaryMissingParams() throws Exception {
    mockMvc.perform(get("/crime/summary")
                    .param("state", "NC"))
            .andExpect(status().isBadRequest());
  }


  /**
   * Test when the API throws exception.
   */
  @Test
  void testGetCrimeSummaryHandlesException() throws Exception {
    try (MockedConstruction<CrimeModel> mocked = Mockito.mockConstruction(
            CrimeModel.class,
            (mock, context) -> when(mock.getCrimeSummary("NC", "V", "10", "2025"))
                    .thenThrow(new RuntimeException()))) {

      mockMvc.perform(get("/crime/summary")
                      .param("state", "NC")
                      .param("offense", "V")
                      .param("month", "10")
                      .param("year", "2025")
                      .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isInternalServerError());
    }
  }

}