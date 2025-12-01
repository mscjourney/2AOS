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

@SpringBootTest
@AutoConfigureMockMvc
public class ClientEndpointsGetUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  /**
   * {@code getClientByIdOk} Verifies existing client id returns 200 and DTO fields.
   */
  @DisplayName("GET /clients/{id} returns 200 when client exists")
  void getClientByIdOk() throws Exception {
    mockMvc.perform(get("/clients/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientId").value(1));
  }

  @Test
  /**
   * {@code getClientByIdNotFound} Confirms missing client id yields 404 and ApiError message.
   */
  @DisplayName("GET /clients/{id} returns 404 when client missing")
  void getClientByIdNotFound() throws Exception {
    mockMvc.perform(get("/clients/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message").value("Client not found."));
  }

  @Test
  /**
   * {@code getClientByIdBadRequest} Validates negative client id produces 400 with message.
   */
  @DisplayName("GET /clients/{id} returns 400 when id negative")
  void getClientByIdBadRequest() throws Exception {
    mockMvc.perform(get("/clients/-5"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Client Id cannot be negative."));
  }
}
