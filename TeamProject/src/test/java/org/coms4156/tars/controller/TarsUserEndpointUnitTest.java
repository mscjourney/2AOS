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
 * Integration tests for TarsUser related GET endpoints.
 * 
 * <p>Equivalence Partition Testing for TarsUser related Endpoints
 * ========== GET /tarsUsers ==========
 * 1) Equivalence Partition 1: There is exactly one TarsUser that exists.
 *
 * <p>Test Cases:
 *
 * <p>2) Equivalence Partition 2: There is more than one TarsUsers that exists.
 *
 * <p>Test Cases:
 *
 * <p>3) Equivalence Partition 3: There are no clients that exist.
 *
 * <p>Test Cases:
 * 
 * <p>========== GET /tarsUsers/{userId} ==========
 * 1) Equivalence Partition 1: userId is non-negative and there is a TarsUser associated with
 *  that userId.
 * 
 * <p>Test Cases:
 * 
 * <p>2) Equivalence Partition 2: userId is non-negative, but there is no TarsUser 
 *  associated with userId.
 *
 * <p>3) Equivalence Partition 3: userId is negative
 * 
 * <p>Test Cases:
 * 
 * <p>========== DELETE /tarsUsers/{userId} ==========
 * 1) Equivalence Partition 1: There exists a TarsUser associated with the userId.
 *
 * <p>Test Cases:
 *
 * <p>2) Equivalence Partition 2: There does not exist a TarsUser associated with the userId.
 *
 * <p>Test Cases:
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TarsUserEndpointUnitTest {

  @Autowired
  private MockMvc mockMvc;

  /**
   * {@code getTarsUserByIdOk} Confirms existing user id returns 200 and
   * the response contains the expected userId.
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 200 when user exists")
  void getTarsUserByIdOk() throws Exception {
    mockMvc.perform(get("/tarsUsers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1));
  }

  /**
   * {@code getTarsUserByIdNotFound} Confirms missing user id returns 404.
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 404 when user missing")
  void getTarsUserByIdNotFound() throws Exception {
    mockMvc.perform(get("/tarsUsers/9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists());
  }

  /**
   * {@code getTarsUserByIdBadRequest} Confirms negative id is rejected with 400.
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 400 when id negative")
  void getTarsUserByIdBadRequest() throws Exception {
    mockMvc.perform(get("/tarsUsers/-10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());
  }
}