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
 * Integration tests for client GET endpoints validating success, not-found and
 * bad-request scenarios with DTO / ApiError payloads.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ClientEndpointsGetUnitTest {

  @Autowired
  private MockMvc mockMvc;

  /**
   * {@code getClientByIdOk} Verifies existing client id returns 200 and DTO fields.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 200 when client exists")
  void getClientByIdOk() throws Exception {
    mockMvc.perform(get("/clients/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientId").value(1));
  }

  /**
   * {@code getClientByIdNotFound} Confirms missing client id yields 404 and ApiError message.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 404 when client missing")
  void getClientByIdNotFound() throws Exception {
    mockMvc.perform(get("/clients/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Client not found."));
  }

  /**
   * {@code getClientByIdBadRequest} Validates negative client id produces 400 with message.
   */
  @Test
  @DisplayName("GET /clients/{id} returns 400 when id negative")
  void getClientByIdBadRequest() throws Exception {
    mockMvc.perform(get("/clients/-5"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Client Id cannot be negative."));
  }
}
