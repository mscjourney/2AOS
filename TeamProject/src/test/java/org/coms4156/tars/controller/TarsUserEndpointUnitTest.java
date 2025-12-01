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
public class TarsUserEndpointUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  /**
   * {@code getTarsUserByIdOk} Confirms existing user id returns 200
   * and the response contains the expected `userId`.
   */
  @DisplayName("GET /tarsUsers/{id} returns 200 when user exists")
  void getTarsUserByIdOk() throws Exception {
    mockMvc.perform(get("/tarsUsers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1));
  }

  @Test
  /**
   * {@code getTarsUserByIdNotFound} Confirms missing user id returns 404
   * and no payload assertions are required.
   */
  @DisplayName("GET /tarsUsers/{id} returns 404 when user missing")
  void getTarsUserByIdNotFound() throws Exception {
    mockMvc.perform(get("/tarsUsers/9999"))
        .andExpect(status().isNotFound());
  }

  @Test
  /**
   * {@code getTarsUserByIdBadRequest} Confirms negative id is rejected
   * with 400 Bad Request.
   */
  @DisplayName("GET /tarsUsers/{id} returns 400 when id negative")
  void getTarsUserByIdBadRequest() throws Exception {
    mockMvc.perform(get("/tarsUsers/-10"))
        .andExpect(status().isBadRequest());
  }

  @Test
  /**
   * {@code getUserByClientIdCases} Validates client-bound user lookup:
   * existing client returns 200; unknown client returns 404; negative id returns 400.
   */
  @DisplayName("GET /user/client/{clientId} returns 200 with prefs or 404 when no user")
  void getUserByClientIdCases() throws Exception {
    // clientId=1 exists with user alice -> should return 200
    mockMvc.perform(get("/user/client/1"))
        .andExpect(status().isOk());

    // large client id likely missing -> 404
    mockMvc.perform(get("/user/client/9999"))
        .andExpect(status().isNotFound());

    // negative -> 400
    mockMvc.perform(get("/user/client/-1"))
        .andExpect(status().isBadRequest());
  }

  @Test
  /**
   * {@code getClientUserListCases} Validates user list retrieval by client:
   * positive id returns 200; negative id returns 400.
   */
  @DisplayName("GET /userList/client/{clientId} returns 200 list or 400 invalid id")
  void getClientUserListCases() throws Exception {
    mockMvc.perform(get("/userList/client/1"))
        .andExpect(status().isOk());

    mockMvc.perform(get("/userList/client/-2"))
        .andExpect(status().isBadRequest());
  }
}
