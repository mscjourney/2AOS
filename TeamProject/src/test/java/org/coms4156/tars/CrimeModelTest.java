package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Unit tests for CrimeModel
 */
class CrimeModelTest {

  private CrimeModel crimeModel;
  private HttpClient mockClient;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    crimeModel = new CrimeModel();
    mockClient = mock(HttpClient.class);
    objectMapper = new ObjectMapper();
  }

  /**
   * Tests successful response with valid paramseters.
   */
  @Test
  void testGetCrimeSummary_Success() {
    CrimeModel model = new CrimeModel();

    String result = model.getCrimeSummary("NC", "V", "10", "2025");

    assertNotNull(result);
    assertTrue(result.contains("North Carolina") && result.contains("cases per 100,000 people"));
  }


  /**
   * Tests when parameters have an invalid state.
   */
  @Test
  void testGetCrimeSummary_NoDataFound() throws Exception {
    CrimeModel model = new CrimeModel();
    String result = model.getCrimeSummary("ZZ", "V", "10", "2025");

    assertNotNull(result);
    assertTrue(result.contains("No rate data available for ZZ"));
  }

  /**
   * Tests when the FBI API returns a non-200 response.
   */
  @Test
  void testGetCrimeSummary_Non200Response() {
    CrimeModel model = new CrimeModel();
    String result = model.getCrimeSummary("NC", "INVALID", "10", "2025");

    assertNotNull(result);
    assertTrue(result.contains("Error") || result.contains("failed"));
  }

  /**
   * Tests handling of exceptions during the API call.
   */
  @Test
  void testGetCrimeSummary_IOException() {
    CrimeModel model = new CrimeModel();
    String result = model.getCrimeSummary("XX", "V", "13", "9999");

    assertNotNull(result);
    assertTrue(result.contains("Error fetching data"));
  }

}

