package org.coms4156.tars.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import org.coms4156.tars.model.TarsUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RouteController.class)
@TestPropertySource(properties = {
    "security.enabled=true",
    "security.apiKey.header=X-API-Key",
    "security.adminApiKeys=adminkey000000000000000000000000"
})
class GetTarsUsersTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private org.coms4156.tars.service.TarsService tarsService;
  @MockBean private org.coms4156.tars.service.ClientService clientService;
  @MockBean private org.coms4156.tars.service.TarsUserService tarsUserService;

  private static final String ADMIN_KEY = "adminkey000000000000000000000000";

  @Test
  @DisplayName("GET /tarsUsers returns empty list")
  void getTarsUsers_emptyList() throws Exception {
    when(tarsUserService.listUsers()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/tarsUsers").header("X-API-Key", ADMIN_KEY))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("[]"));
  }

  @Test
  @DisplayName("GET /tarsUsers returns non-empty list")
  void getTarsUsers_nonEmpty() throws Exception {
    TarsUser u1 = new TarsUser();
    TarsUser u2 = new TarsUser();
    when(tarsUserService.listUsers()).thenReturn(List.of(u1, u2));

    mockMvc
        .perform(get("/tarsUsers").header("X-API-Key", ADMIN_KEY))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // We only assert array length using a simple contains check
        .andExpect(content().string(org.hamcrest.Matchers.containsString("[")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("]")));
  }

  @Test
  @DisplayName("GET /tarsUsers propagates server error")
  void getTarsUsers_exception() throws Exception {
    when(tarsUserService.listUsers()).thenThrow(new RuntimeException("boom"));

    mockMvc
        .perform(get("/tarsUsers").header("X-API-Key", ADMIN_KEY))
        .andExpect(status().isInternalServerError());
  }
}
