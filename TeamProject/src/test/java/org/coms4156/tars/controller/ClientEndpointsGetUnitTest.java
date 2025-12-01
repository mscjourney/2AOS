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
  @DisplayName("GET /clients/{id} returns 200 when client exists")
  void getClientById_ok() throws Exception {
    mockMvc.perform(get("/clients/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.clientId").value(1));
  }

  @Test
  @DisplayName("GET /clients/{id} returns 404 when client missing")
  void getClientById_notFound() throws Exception {
    mockMvc.perform(get("/clients/999"))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("GET /clients/{id} returns 400 when id negative")
  void getClientById_badRequest() throws Exception {
    mockMvc.perform(get("/clients/-5"))
        .andExpect(status().isBadRequest());
  }
}
