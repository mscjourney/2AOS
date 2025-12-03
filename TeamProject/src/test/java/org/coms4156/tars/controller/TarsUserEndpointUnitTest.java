package org.coms4156.tars.controller;

import java.util.ArrayList;
import java.util.List;

import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.service.TarsUserService;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * Endpoint tests for TarsUser related GET endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TarsUserEndpointUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TarsUserService tarsUserService;

  /* ========== GET /tarsUsers/{userId} Equivalence Partition ========== */

  /**
   * {@code getTarsUserByIdOk} Confirms existing user id returns 200 and
   * the response contains the expected userId.
   * Equivalence Partition 1: userId is non-negative and there is a TarsUser associated with
   *  that userId.
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 200 when user exists")
  void getTarsUserByIdOk() throws Exception {
    mockMvc.perform(get("/tarsUsers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.username").value("alice"));

    mockMvc.perform(get("/tarsUsers/4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(4))
        .andExpect(jsonPath("$.username").value("jdoe"));
  }

  /**
   * {@code getTarsUserByIdNotFound} Confirms missing user id returns 404.
   * Equivalence Partition 2: userId is non-negative, but there is no TarsUser 
   *  associated with userId.
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 404 when user missing")
  void getTarsUserByIdNotFound() throws Exception {
    mockMvc.perform(get("/tarsUsers/0"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists());
    mockMvc.perform(get("/tarsUsers/9999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.message").exists());
  }

  /**
   * {@code getTarsUserByIdBadRequest} Confirms negative id is rejected with 400.
   * Equivalence Partition 3: userId is negative
   */
  @Test
  @DisplayName("GET /tarsUsers/{id} returns 400 when id negative")
  void getTarsUserByIdBadRequest() throws Exception {
    mockMvc.perform(get("/tarsUsers/-1"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());
    mockMvc.perform(get("/tarsUsers/-10"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.message").exists());
  }

  /* ========== GET /tarsUsers ========== */
  
  /**
   * {@code testGetTarsUserListNonEmpty}
   * Equivalence Partition 1: There is one or more TarsUser that exists.
   */
  @Test
  void testGetTarsUserListNonEmpty() throws Exception {
    mockMvc.perform(get("/tarsUsers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(4)));
  }

  /**
   * {@code testGetTarsUserEmpty}
   * Equivalence Partition 2: There are no TarsUsers that exist.
   */
  @Test
  void testGetTarsUserEmpty() throws Exception {
  }


  /* ========== DELETE /tarsUsers/{userId} ========== */

  /**
   * {@code }
   * Equivalence Partition 1: There exists a TarsUser associated with the userId.
   */
  @Test
  void testDeleteExistingUser() throws Exception {
  }

  /**
   * {@code testDeleteNonExistingUser}
   * Equivalence Partition 2: There does not exist a TarsUser associated with the userId.
   */
  @Test
  void testDeleteNonExistingUser() throws Exception {

  }
}