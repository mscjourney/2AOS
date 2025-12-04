package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


/**
 * Endpoint tests for TarsUser related GET endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TarsUserEndpointUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsUserService tarsUserService;

  @BeforeEach
  void setUp() throws Exception {
    List<TarsUser> userList = new ArrayList<>();

    TarsUser user1 = new TarsUser(1L, "alice", "alice@gmail.com", "user");
    user1.setUserId(1L);
    userList.add(user1);

    TarsUser user4 = new TarsUser(4L, "jdoe", "jdoe@gmail.com", "user");
    user4.setUserId(4L);
    userList.add(user4);

    when(tarsUserService.listUsers()).thenReturn(userList);
    when(tarsUserService.findById(1L)).thenReturn(user1);
    when(tarsUserService.findById(4L)).thenReturn(user4);
    when(tarsUserService.deleteUser(1L)).thenReturn(user1);
    when(tarsUserService.deleteUser(4L)).thenReturn(user4);
  }
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
        .andExpect(jsonPath("$", hasSize(2)));
  }

  /**
   * {@code testGetTarsUserEmpty}
   * Equivalence Partition 2: There are no TarsUsers that exist.
   */
  @Test
  void testGetTarsUserEmpty() throws Exception {
    when(tarsUserService.listUsers()).thenReturn(new ArrayList<>());

    mockMvc.perform(get("/tarsUsers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }


  /* ========== DELETE /tarsUsers/{userId} ========== */

  /**
   * {@code }
   * Equivalence Partition 1: There exists a TarsUser associated with the userId.
   *    We do not care about the value of userId (non-negative vs negative) as
   *    it comes down to whether the TarsUser exists or not.
   */
  @Test
  void testDeleteExistingUser() throws Exception {
    mockMvc.perform(delete("/tarsUsers/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.username").value("alice"));

    mockMvc.perform(delete("/tarsUsers/4"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(4))
        .andExpect(jsonPath("$.username").value("jdoe"));
  }

  /**
   * {@code testDeleteNonExistingUser}
   * Equivalence Partition 2: There does not exist a TarsUser associated with the userId.
   */
  @Test
  void testDeleteNonExistingUser() throws Exception {
    mockMvc.perform(delete("/tarsUsers/-1"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User not found"));
    
    mockMvc.perform(delete("/tarsUsers/2"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User not found"));
    
    mockMvc.perform(delete("/tarsUsers/3"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User not found"));
  }
}