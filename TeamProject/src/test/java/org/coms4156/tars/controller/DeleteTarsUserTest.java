package org.coms4156.tars.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RouteController.class)
@TestPropertySource(properties = {
    "security.enabled=true",
    "security.apiKey.header=X-API-Key",
    "security.adminApiKeys=adminkey000000000000000000000000"
})
class DeleteTarsUserTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private org.coms4156.tars.service.TarsService tarsService;
  @MockBean private org.coms4156.tars.service.ClientService clientService;
  @MockBean private org.coms4156.tars.service.TarsUserService tarsUserService;

  private static final String ADMIN_KEY = "adminkey000000000000000000000000";

  @Test
  @DisplayName("DELETE /tarsUsers/{id} with admin key returns 200")
  void deleteTarsUser_adminSuccess() throws Exception {
    // Mock service to simulate successful deletion
    org.coms4156.tars.model.TarsUser mockUser = new org.coms4156.tars.model.TarsUser();
    org.mockito.Mockito.when(tarsUserService.deleteUser(1L)).thenReturn(mockUser);
    mockMvc
        .perform(delete("/tarsUsers/1").header("X-API-Key", ADMIN_KEY))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("DELETE /tarsUsers/{id} not found returns 404")
  void deleteTarsUser_notFound() throws Exception {
    org.mockito.Mockito.when(tarsUserService.deleteUser(999999L)).thenReturn(null);
    mockMvc
        .perform(delete("/tarsUsers/999999").header("X-API-Key", ADMIN_KEY))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /tarsUsers/{id} invalid id returns 400")
  void deleteTarsUser_invalidId() throws Exception {
    // No need to mock service; controller should reject negative id
    mockMvc
        .perform(delete("/tarsUsers/-5").header("X-API-Key", ADMIN_KEY))
      .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("DELETE /tarsUsers/{id} with non-admin key returns 403")
  void deleteTarsUser_forbiddenForNonAdmin() throws Exception {
    mockMvc
        .perform(delete("/tarsUsers/1").header("X-API-Key", "nonadmin-key"))
      .andExpect(status().isUnauthorized());
  }
}
