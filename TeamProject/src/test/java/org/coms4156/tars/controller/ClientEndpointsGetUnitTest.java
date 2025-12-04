package org.coms4156.tars.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code ClientEndpointsGetUnitTest}
 * Integration tests for client GET endpoints validating success, not-found and
 * bad-request scenarios with DTO / ApiError payloads.
 */
@SpringBootTest(properties = {
  "security.enabled=true",
  "security.adminApiKeys=adminkey000000000000000000000000",
  "security.apiKey.header=X-API-Key"
})
@AutoConfigureMockMvc
public class ClientEndpointsGetUnitTest {

  @Autowired
  private MockMvc mockMvc;

  /* ======== GET /clients/{clientId} Equivalence Partitions ======= */

  /**
   * {@code getClientByIdOk} Verifies existing client id returns 200 and DTO fields.
   * Equivalence Partition 1: clientId is non-negative and there is an existing client
   *   associated with that id.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 200 when client exists")
  void getClientByIdOk() throws Exception {
    mockMvc.perform(get("/clients/1").header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientId").value(1));
  }

  /**
   * {@code getClientByIdNotFound} Confirms missing client id yields 404 and ApiError message.
   * Equivalence Partition 2: clientId is non-negative but there is no existing client
   *    associated with that id.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 404 when client missing")
  void getClientByIdNotFound() throws Exception {
    mockMvc.perform(get("/clients/999").header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists());
  }

  /**
   * {@code getClientByIdBadRequest} Validates negative client id produces 400 with message.
   * Equivalence Partition 3: clientId is negative.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 400 when id negative")
  void getClientByIdBadRequest() throws Exception {
    mockMvc.perform(get("/clients/-5").header("X-API-Key", "adminkey000000000000000000000000"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.message").exists());
  }

  /**
   * {@code getClientsTest} Returns a list of all clients registered under the service.
   */
  @Test
  @DisplayName("GET /clients returns 200 on success")
  public void getClientsTestMultiple() throws Exception {
    mockMvc.perform(get("/clients").header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].clientId").exists())
        .andExpect(jsonPath("$[0].clientId").value(1))
        .andExpect(jsonPath("$[1].clientId").value(2))
        .andExpect(jsonPath("$[2].clientId").value(3))
        .andExpect(jsonPath("$[3].clientId").value(4))
        .andExpect(jsonPath("$[4].clientId").value(5))
        .andExpect(jsonPath("$[5].clientId").value(6));
  }
}
