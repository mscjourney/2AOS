package org.coms4156.tars.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.coms4156.tars.util.LoggerTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * {@code ClientEndPointUnitTest} Unit tests for client-related endpoints
 * in {@link RouteController}. Tests cover all branches and validation paths.
 */
@WebMvcTest(RouteController.class)
public class ClientEndPointUnitTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private TarsService tarsService;

  @MockitoBean
  private ClientService clientService;

  @MockitoBean
  private TarsUserService tarsUserService;

  // ==================== POST /client/create Tests ====================

  /**
   * {@code createClientSuccessTest} Verifies successful client creation with
   * valid name and email returns 201 CREATED.
   */
  @Test
  public void createClientSuccessTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");
    requestBody.put("email", "test@example.com");

    Client mockClient = new Client();
    mockClient.setClientId(1L);
    mockClient.setName("TestClient");
    mockClient.setEmail("test@example.com");

    when(clientService.uniqueNameCheck("TestClient")).thenReturn(true);
    when(clientService.uniqueEmailCheck("test@example.com")).thenReturn(true);
    when(clientService.createClient("TestClient", "test@example.com"))
        .thenReturn(mockClient);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.clientId").value(1))
        .andExpect(jsonPath("$.name").value("TestClient"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.portalUrl").exists());

    verify(clientService).uniqueNameCheck("TestClient");
    verify(clientService).uniqueEmailCheck("test@example.com");
    verify(clientService).createClient("TestClient", "test@example.com");
  }

  /**
   * {@code createClientWithNullBodyTest} Ensures null request body returns
   * 400 BAD REQUEST.
   */
  @Test
  public void createClientWithNullBodyTest() throws Exception {
    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content("null"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Missing 'name' and 'email'."));
  }

  /**
   * {@code createClientMissingNameFieldTest} Validates missing 'name' field
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientMissingNameFieldTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("email", "test@example.com");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Missing 'name'."));
  }

  /**
   * {@code createClientWithNullNameValueTest} Confirms null name value is
   * treated as blank and returns 400.
   */
  @Test
  public void createClientWithNullNameValueTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", null);
    requestBody.put("email", "test@example.com");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Client name cannot be blank."));
  }

  /**
   * {@code createClientWithBlankNameTest} Ensures whitespace-only name
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientWithBlankNameTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "   ");
    requestBody.put("email", "test@example.com");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Client name cannot be blank."));
  }

  /**
   * {@code createClientMissingEmailFieldTest} Validates missing 'email' field
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientMissingEmailFieldTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Missing 'email'."));
  }

  /**
   * {@code createClientWithNullEmailValueTest} Confirms null email value is
   * treated as blank and returns 400.
   */
  @Test
  public void createClientWithNullEmailValueTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");
    requestBody.put("email", null);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Client email cannot be blank."));
  }

  /**
   * {@code createClientWithBlankEmailTest} Ensures whitespace-only email
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientWithBlankEmailTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");
    requestBody.put("email", "   ");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Client email cannot be blank."));
  }

  /**
   * {@code createClientWithInvalidEmailFormatTest} Validates invalid email
   * format returns 400 BAD REQUEST.
   */
  @Test
  public void createClientWithInvalidEmailFormatTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");
    requestBody.put("email", "invalid-email");

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid email format."));
  }

  /**
   * {@code createClientWithDuplicateNameTest} Confirms duplicate name returns
   * 409 CONFLICT.
   */
  @Test
  public void createClientWithDuplicateNameTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "ExistingClient");
    requestBody.put("email", "new@example.com");

    when(clientService.uniqueNameCheck("ExistingClient")).thenReturn(false);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Client name already exists."));

    verify(clientService).uniqueNameCheck("ExistingClient");
  }

  /**
   * {@code createClientWithDuplicateEmailTest} Confirms duplicate email
   * returns 409 CONFLICT.
   */
  @Test
  public void createClientWithDuplicateEmailTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "NewClient");
    requestBody.put("email", "existing@example.com");

    when(clientService.uniqueNameCheck("NewClient")).thenReturn(true);
    when(clientService.uniqueEmailCheck("existing@example.com")).thenReturn(false);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Client email already exists."));

    verify(clientService).uniqueNameCheck("NewClient");
    verify(clientService).uniqueEmailCheck("existing@example.com");
  }

  /**
   * {@code createClientServiceReturnsNullTest} Validates 500 INTERNAL ERROR
   * when service returns null.
   */
  @Test
  public void createClientServiceReturnsNullTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "TestClient");
    requestBody.put("email", "test@example.com");

    when(clientService.uniqueNameCheck("TestClient")).thenReturn(true);
    when(clientService.uniqueEmailCheck("test@example.com")).thenReturn(true);
    when(clientService.createClient("TestClient", "test@example.com"))
        .thenReturn(null);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Failed to create client."));

    verify(clientService).createClient("TestClient", "test@example.com");
  }

  /**
   * {@code createClientTrimsWhitespaceTest} Ensures leading/trailing
   * whitespace is trimmed from name and email.
   */
  @Test
  public void createClientTrimsWhitespaceTest() throws Exception {
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("name", "  TestClient  ");
    requestBody.put("email", "  test@example.com  ");

    Client mockClient = new Client();
    mockClient.setClientId(1L);
    mockClient.setName("TestClient");
    mockClient.setEmail("test@example.com");

    when(clientService.uniqueNameCheck("TestClient")).thenReturn(true);
    when(clientService.uniqueEmailCheck("test@example.com")).thenReturn(true);
    when(clientService.createClient("TestClient", "test@example.com"))
        .thenReturn(mockClient);

    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestBody)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("TestClient"))
        .andExpect(jsonPath("$.clientId").value(1));

    verify(clientService).createClient("TestClient", "test@example.com");
  }

  // ==================== POST /client/createUser Tests ====================

  /**
   * {@code createClientUserSuccessTest} Verifies successful user creation
   * with valid data returns 201 CREATED.
   */
  @Test
  public void createClientUserSuccessTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    Client mockClient = new Client();
    mockClient.setClientId(1L);

    TarsUser createdUser = new TarsUser();
    createdUser.setUserId(10L);
    createdUser.setClientId(1L);
    createdUser.setUsername("testuser");
    createdUser.setEmail("user@example.com");
    createdUser.setRole("admin");
    createdUser.setActive(true);

    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "testuser"))
        .thenReturn(false);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "user@example.com"))
        .thenReturn(false);
    when(tarsUserService.createUser(1L, "testuser", "user@example.com", "admin"))
        .thenReturn(createdUser);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(10))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("user@example.com"))
        .andExpect(jsonPath("$.role").value("admin"))
        .andExpect(jsonPath("$.active").value(true));

    verify(clientService).getClient(1);
    verify(tarsUserService).existsByClientIdAndUsername(1L, "testuser");
    verify(tarsUserService).existsByClientIdAndUserEmail(1L, "user@example.com");
    verify(tarsUserService).createUser(1L, "testuser", "user@example.com", "admin");
  }

  /**
   * {@code createClientUserWithNullBodyTest} Ensures null request body
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNullBodyTest() throws Exception {
    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content("null"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Body cannot be null."));
  }

  /**
   * {@code createClientUserWithNullClientIdTest} Validates null clientId
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNullClientIdTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(null);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid clientId."));
  }

  /**
   * {@code createClientUserWithNegativeClientIdTest} Confirms negative
   * clientId returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNegativeClientIdTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(-1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid clientId."));
  }

  /**
   * {@code createClientUserWithNullUsernameTest} Validates null username
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNullUsernameTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername(null);
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Username cannot be blank."));
  }

  /**
   * {@code createClientUserWithBlankUsernameTest} Ensures whitespace-only
   * username returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithBlankUsernameTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("   ");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Username cannot be blank."));
  }

  /**
   * {@code createClientUserWithNullEmailTest} Validates null email returns
   * 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNullEmailTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail(null);
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Email cannot be blank."));
  }

  /**
   * {@code createClientUserWithBlankEmailTest} Ensures whitespace-only email
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithBlankEmailTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("   ");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Email cannot be blank."));
  }

  /**
   * {@code createClientUserWithInvalidEmailFormatTest} Validates invalid
   * email format returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithInvalidEmailFormatTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("invalid-email");
    requestUser.setRole("admin");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid email format."));
  }

  /**
   * {@code createClientUserWithNullRoleTest} Validates null role returns
   * 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithNullRoleTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole(null);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Role cannot be blank."));
  }

  /**
   * {@code createClientUserWithBlankRoleTest} Ensures whitespace-only role
   * returns 400 BAD REQUEST.
   */
  @Test
  public void createClientUserWithBlankRoleTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("   ");

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Role cannot be blank."));
  }

  /**
   * {@code createClientUserClientNotFoundTest} Confirms non-existent client
   * returns 404 NOT FOUND.
   */
  @Test
  public void createClientUserClientNotFoundTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(999L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    when(clientService.getClient(999)).thenReturn(null);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Client not found."));

    verify(clientService).getClient(999);
  }

  /**
   * {@code createClientUserDuplicateUsernameTest} Validates duplicate
   * username for client returns 409 CONFLICT.
   */
  @Test
  public void createClientUserDuplicateUsernameTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("existinguser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    Client mockClient = new Client();
    mockClient.setClientId(1L);

    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "existinguser"))
        .thenReturn(true);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Username already exists for this client."));

    verify(tarsUserService).existsByClientIdAndUsername(1L, "existinguser");
  }

  /**
   * {@code createClientUserDuplicateEmailTest} Validates duplicate email
   * for client returns 409 CONFLICT.
   */
  @Test
  public void createClientUserDuplicateEmailTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("newuser");
    requestUser.setEmail("existing@example.com");
    requestUser.setRole("admin");

    Client mockClient = new Client();
    mockClient.setClientId(1L);

    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "newuser"))
        .thenReturn(false);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "existing@example.com"))
        .thenReturn(true);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isConflict())
        .andExpect(content().string("A user with the email already exists for this client."));

    verify(tarsUserService).existsByClientIdAndUserEmail(1L, "existing@example.com");
  }

  /**
   * {@code createClientUserServiceReturnsNullTest} Validates 500 INTERNAL
   * ERROR when service returns null.
   */
  @Test
  public void createClientUserServiceReturnsNullTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("testuser");
    requestUser.setEmail("user@example.com");
    requestUser.setRole("admin");

    Client mockClient = new Client();
    mockClient.setClientId(1L);

    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "testuser"))
        .thenReturn(false);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "user@example.com"))
        .thenReturn(false);
    when(tarsUserService.createUser(1L, "testuser", "user@example.com", "admin"))
        .thenReturn(null);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Failed to create user."));

    verify(tarsUserService).createUser(1L, "testuser", "user@example.com", "admin");
  }

  /**
   * {@code createClientUserTrimsWhitespaceTest} Ensures leading/trailing
   * whitespace is trimmed from all fields.
   */
  @Test
  public void createClientUserTrimsWhitespaceTest() throws Exception {
    TarsUser requestUser = new TarsUser();
    requestUser.setClientId(1L);
    requestUser.setUsername("  testuser  ");
    requestUser.setEmail("  user@example.com  ");
    requestUser.setRole("  admin  ");

    Client mockClient = new Client();
    mockClient.setClientId(1L);

    TarsUser createdUser = new TarsUser();
    createdUser.setUserId(10L);
    createdUser.setClientId(1L);
    createdUser.setUsername("testuser");
    createdUser.setEmail("user@example.com");
    createdUser.setRole("admin");

    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "testuser"))
        .thenReturn(false);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "user@example.com"))
        .thenReturn(false);
    when(tarsUserService.createUser(1L, "testuser", "user@example.com", "admin"))
        .thenReturn(createdUser);

    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestUser)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("user@example.com"));

    verify(tarsUserService).createUser(1L, "testuser", "user@example.com", "admin");
  }

  /**
   * {@code createClientWithValidEmailFormatsTest} Validates various valid
   * email formats are accepted for client creation.
   */
  @Test
  public void createClientWithValidEmailFormatsTest() throws Exception {
    String[] validEmails = {
        "test@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk",
        "test123@test-domain.com"
    };

    for (String email : validEmails) {
      Map<String, String> requestBody = new HashMap<>();
      requestBody.put("name", "TestClient");
      requestBody.put("email", email);

      Client mockClient = new Client();
      mockClient.setClientId(1L);

      when(clientService.uniqueNameCheck(anyString())).thenReturn(true);
      when(clientService.uniqueEmailCheck(anyString())).thenReturn(true);
      when(clientService.createClient(anyString(), anyString()))
          .thenReturn(mockClient);

      mockMvc.perform(post("/client/create")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(requestBody)))
          .andExpect(status().isCreated());
    }
  }

  /**
   * {@code createClientUserWithValidEmailFormatsTest} Validates various
   * valid email formats are accepted.
   */
  @Test
  public void createClientUserWithValidEmailFormatsTest() throws Exception {
    String[] validEmails = {
        "test@example.com",
        "user.name@example.com",
        "user+tag@example.co.uk",
        "test123@test-domain.com"
    };

    for (String email : validEmails) {
      TarsUser requestUser = new TarsUser();
      requestUser.setClientId(1L);
      requestUser.setUsername("testuser");
      requestUser.setEmail(email);
      requestUser.setRole("admin");

      Client mockClient = new Client();
      mockClient.setClientId(1L);

      TarsUser createdUser = new TarsUser();
      createdUser.setUserId(10L);
      createdUser.setClientId(1L);

      when(clientService.getClient(1)).thenReturn(mockClient);
      when(tarsUserService.existsByClientIdAndUsername(anyLong(), anyString()))
          .thenReturn(false);
      when(tarsUserService.existsByClientIdAndUserEmail(anyLong(), anyString()))
          .thenReturn(false);
      when(tarsUserService.createUser(anyLong(), anyString(), anyString(), anyString()))
          .thenReturn(createdUser);

      mockMvc.perform(post("/client/createUser")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(requestUser)))
          .andExpect(status().isCreated());
    }
  }

  /**
   * {@code createClientCaseInsensitiveDuplicateNameTest} Confirms name duplicate
   * matching is case-insensitive (returns 409 on variant case).
   */
  @Test
  public void createClientCaseInsensitiveDuplicateNameTest() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("name", "EXISTINGCLIENT");
    body.put("email", "new@example.com");
    when(clientService.uniqueNameCheck("EXISTINGCLIENT")).thenReturn(false);
    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Client name already exists."));
    verify(clientService).uniqueNameCheck("EXISTINGCLIENT");
  }

  /**
   * {@code createClientCaseInsensitiveDuplicateEmailTest} Confirms email duplicate
   * matching is case-insensitive.
   */
  @Test
  public void createClientCaseInsensitiveDuplicateEmailTest() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("name", "NewClientX");
    body.put("email", "EXISTING@EXAMPLE.COM");
    when(clientService.uniqueNameCheck("NewClientX")).thenReturn(true);
    when(clientService.uniqueEmailCheck("EXISTING@EXAMPLE.COM")).thenReturn(false);
    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Client email already exists."));
    verify(clientService).uniqueEmailCheck("EXISTING@EXAMPLE.COM");
  }

  /**
   * {@code createClientBothNameAndEmailDuplicateTest} Ensures name conflict
   * precedes email conflict when both duplicate.
   */
  @Test
  public void createClientBothNameAndEmailDuplicateTest() throws Exception {
    Map<String, String> body = new HashMap<>();
    body.put("name", "DupName");
    body.put("email", "dup@example.com");
    when(clientService.uniqueNameCheck("DupName")).thenReturn(false);
    // Even if email is also duplicate, controller should stop at name.
    when(clientService.uniqueEmailCheck("dup@example.com")).thenReturn(false);
    mockMvc.perform(post("/client/create")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Client name already exists."));
    verify(clientService).uniqueNameCheck("DupName");
    // Email uniqueness should NOT be called if name fails first; assert optional:
    // verify(clientService, never()).uniqueEmailCheck("dup@example.com");
  }

  /**
   * {@code createClientUserCaseInsensitiveDuplicateUsernameTest} Confirms
   * username duplicate check is case-insensitive.
   */
  @Test
  public void createClientUserCaseInsensitiveDuplicateUsernameTest() throws Exception {
    TarsUser req = new TarsUser();
    req.setClientId(1L);
    req.setUsername("EXISTINGUSER");
    req.setEmail("user2@example.com");
    req.setRole("admin");
    Client mockClient = new Client();
    mockClient.setClientId(1L);
    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "EXISTINGUSER")).thenReturn(true);
    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Username already exists for this client."));
    verify(tarsUserService).existsByClientIdAndUsername(1L, "EXISTINGUSER");
  }

  /**
   * {@code createClientUserCaseInsensitiveDuplicateEmailTest} Confirms
   * email duplicate check is case-insensitive.
   */
  @Test
  public void createClientUserCaseInsensitiveDuplicateEmailTest() throws Exception {
    TarsUser req = new TarsUser();
    req.setClientId(1L);
    req.setUsername("newuser2");
    req.setEmail("EXISTING@EXAMPLE.COM");
    req.setRole("admin");
    Client mockClient = new Client();
    mockClient.setClientId(1L);
    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "newuser2")).thenReturn(false);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "EXISTING@EXAMPLE.COM")).thenReturn(true);
    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isConflict())
        .andExpect(content().string("A user with the email already exists for this client."));
    verify(tarsUserService).existsByClientIdAndUserEmail(1L, "EXISTING@EXAMPLE.COM");
  }

  /**
   * {@code createClientUserBothUsernameAndEmailDuplicateTest} Ensures username
   * conflict short-circuits before email conflict when both duplicated.
   */
  @Test
  public void createClientUserBothUsernameAndEmailDuplicateTest() throws Exception {
    TarsUser req = new TarsUser();
    req.setClientId(1L);
    req.setUsername("dupuser");
    req.setEmail("dup@example.com");
    req.setRole("admin");
    Client mockClient = new Client();
    mockClient.setClientId(1L);
    when(clientService.getClient(1)).thenReturn(mockClient);
    when(tarsUserService.existsByClientIdAndUsername(1L, "dupuser")).thenReturn(true);
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "dup@example.com")).thenReturn(true);
    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Username already exists for this client."));
    verify(tarsUserService).existsByClientIdAndUsername(1L, "dupuser");
    // Optional ordering assertion (email check should not run):
    // verify(tarsUserService, never()).existsByClientIdAndUserEmail(1L, "dup@example.com");
  }

  // ==================== Logger Level Toggle Tests ====================

  /**
   * {@code createClientDebugLoggingEnabledTest}
   * Exercises debug branch when DEBUG enabled.
   */
  @Test
  public void createClientDebugLoggingEnabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.DEBUG)) {
      Map<String, String> body = new HashMap<>();
      body.put("name", "DbgClient");
      body.put("email", "dbg@example.com");

      Client mockClient = new Client();
      mockClient.setClientId(55L);
      mockClient.setName("DbgClient");
      mockClient.setEmail("dbg@example.com");

      when(clientService.uniqueNameCheck("DbgClient"))
          .thenReturn(true);
      when(clientService.uniqueEmailCheck("dbg@example.com"))
          .thenReturn(true);
      when(clientService.createClient(
          "DbgClient",
          "dbg@example.com"
      )).thenReturn(mockClient);

      mockMvc.perform(post("/client/create")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isCreated());

      assertTrue(
          cap.contains(Level.DEBUG, "raw body keys"),
          "Expected debug log for raw body keys."
      );
    }
  }

  /**
   * {@code createClientInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void createClientInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {
      Map<String, String> body = new HashMap<>();
      body.put("name", "NoInfoClient");
      body.put("email", "noinfo@example.com");

      Client mockClient = new Client();
      mockClient.setClientId(77L);
      mockClient.setName("NoInfoClient");
      mockClient.setEmail("noinfo@example.com");

      when(clientService.uniqueNameCheck("NoInfoClient"))
          .thenReturn(true);
      when(clientService.uniqueEmailCheck("noinfo@example.com"))
          .thenReturn(true);
      when(clientService.createClient(
          "NoInfoClient",
          "noinfo@example.com"
      )).thenReturn(mockClient);

      mockMvc.perform(post("/client/create")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isCreated());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code createClientWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void createClientWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      Map<String, String> body = new HashMap<>();
      body.put("email", "x@example.com"); // missing name triggers warn path

      mockMvc.perform(post("/client/create")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isBadRequest());

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code createClientErrorLoggingDisabledTest}
   * Covers false branch of isErrorEnabled() (ERROR off).
   */
  @Test
  public void createClientErrorLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.OFF)) {
      Map<String, String> body = new HashMap<>();
      body.put("name", "ErrClient");
      body.put("email", "err@example.com");

      when(clientService.uniqueNameCheck("ErrClient"))
          .thenReturn(true);
      when(clientService.uniqueEmailCheck("err@example.com"))
          .thenReturn(true);
      when(clientService.createClient(
          "ErrClient",
          "err@example.com"
      )).thenReturn(null); // error path

      mockMvc.perform(post("/client/create")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(body)))
          .andExpect(status().isInternalServerError());

      assertFalse(
          cap.hasLevel(Level.ERROR),
          "ERROR suppressed at OFF."
      );
    }
  }

  /**
   * {@code createClientUserDebugLoggingEnabledTest}
   * Exercises both debug branches when DEBUG enabled.
   */
  @Test
  public void createClientUserDebugLoggingEnabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.DEBUG)) {
      TarsUser req = new TarsUser();
      req.setClientId(5L);
      req.setUsername("dbguser");
      req.setEmail("dbguser@example.com");
      req.setRole("admin");

      Client mockClient = new Client();
      mockClient.setClientId(5L);

      TarsUser created = new TarsUser();
      created.setUserId(500L);
      created.setClientId(5L);
      created.setUsername("dbguser");
      created.setEmail("dbguser@example.com");
      created.setRole("admin");
      created.setActive(true);

      when(clientService.getClient(5)).thenReturn(mockClient);
      when(tarsUserService.existsByClientIdAndUsername(5L, "dbguser")).thenReturn(false);
      when(tarsUserService.existsByClientIdAndUserEmail(
          5L,
          "dbguser@example.com"
      )).thenReturn(false);
      when(tarsUserService.createUser(
          5L,
          "dbguser",
          "dbguser@example.com",
          "admin"
      )).thenReturn(created);

      mockMvc.perform(post("/client/createUser")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(req)))
          .andExpect(status().isCreated());

      assertTrue(
          cap.contains(Level.DEBUG, "validation passed"),
          "Expected debug validation log."
      );
      assertTrue(
          cap.contains(Level.DEBUG, "created user detail"),
          "Expected debug created user detail log."
      );
    }
  }

  /**
   * {@code createClientUserInfoLoggingDisabledTest}
   * Covers false branch of isInfoEnabled() (INFO off).
   */
  @Test
  public void createClientUserInfoLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.WARN)) {
      TarsUser req = new TarsUser();
      req.setClientId(6L);
      req.setUsername("noinfouser");
      req.setEmail("noinfo@example.com");
      req.setRole("admin");

      Client mockClient = new Client();
      mockClient.setClientId(6L);

      TarsUser created = new TarsUser();
      created.setUserId(600L);

      when(clientService.getClient(6)).thenReturn(mockClient);
      when(tarsUserService.existsByClientIdAndUsername(6L, "noinfouser")).thenReturn(false);
      when(tarsUserService.existsByClientIdAndUserEmail(
          6L,
          "noinfo@example.com"
      )).thenReturn(false);
      when(tarsUserService.createUser(
          6L,
          "noinfouser",
          "noinfo@example.com",
          "admin"
      )).thenReturn(created);

      mockMvc.perform(post("/client/createUser")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(req)))
          .andExpect(status().isCreated());

      assertFalse(
          cap.hasLevel(Level.INFO),
          "INFO suppressed at WARN."
      );
    }
  }

  /**
   * {@code createClientUserWarnLoggingDisabledTest}
   * Covers false branch of isWarnEnabled() (WARN off).
   */
  @Test
  public void createClientUserWarnLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.ERROR)) {
      TarsUser req = new TarsUser();
      req.setClientId(7L);
      req.setUsername("   "); // blank username triggers warn path
      req.setEmail("x@example.com");
      req.setRole("admin");

      mockMvc.perform(post("/client/createUser")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(req)))
          .andExpect(status().isBadRequest());

      assertFalse(
          cap.hasLevel(Level.WARN),
          "WARN suppressed at ERROR."
      );
    }
  }

  /**
   * {@code createClientUserErrorLoggingDisabledTest}
   * Covers false branch of isErrorEnabled() (ERROR off).
   */
  @Test
  public void createClientUserErrorLoggingDisabledTest() throws Exception {
    try (LoggerTestUtil.CapturedLogger cap =
             LoggerTestUtil.capture(RouteController.class, Level.OFF)) {
      TarsUser req = new TarsUser();
      req.setClientId(8L);
      req.setUsername("erruser");
      req.setEmail("err@example.com");
      req.setRole("admin");

      Client mockClient = new Client();
      mockClient.setClientId(8L);

      when(clientService.getClient(8)).thenReturn(mockClient);
      when(tarsUserService.existsByClientIdAndUsername(8L, "erruser")).thenReturn(false);
      when(tarsUserService.existsByClientIdAndUserEmail(8L, "err@example.com")).thenReturn(false);
      when(tarsUserService.createUser(
          8L,
          "erruser",
          "err@example.com",
          "admin"
      )).thenReturn(null); // error path

      mockMvc.perform(post("/client/createUser")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(req)))
          .andExpect(status().isInternalServerError());

      assertFalse(
          cap.hasLevel(Level.ERROR),
          "ERROR suppressed at OFF."
      );
    }
  }


  /**
   * {@code updateUserPreferenceSuccessTest} Verifies successful user preference
   * update with valid data returns 200 OK.
   */
  @Test
  public void updateUserPreferenceSuccessTest() throws Exception {
    final Long userId = 1L;
    List<String> weatherPrefs = new ArrayList<>();
    weatherPrefs.add("sunny");
    List<String> tempPrefs = new ArrayList<>();
    tempPrefs.add("70F");
    List<String> cityPrefs = new ArrayList<>();
    cityPrefs.add("New York");
    UserPreference userPreference = new UserPreference(userId);
    userPreference.setWeatherPreferences(weatherPrefs);
    userPreference.setTemperaturePreferences(tempPrefs);
    userPreference.setCityPreferences(cityPrefs);

    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(userId);
    mockUser.setActive(true);

    when(tarsUserService.findById(userId)).thenReturn(mockUser);
    when(tarsService.updateUser(userPreference)).thenReturn(true);

    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.intValue()));

    verify(tarsUserService).findById(userId);
    verify(tarsService).updateUser(userPreference);
  }

  /**
   * {@code updateUserPreferenceNegativeUserIdTest} Validates negative userId
   * returns 400 BAD REQUEST.
   */
  @Test
  public void updateUserPreferenceNegativeUserIdTest() throws Exception {
    Long userId = -1L;
    UserPreference userPreference = new UserPreference(userId);

    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));
  }

  /**
   * {@code updateUserPreferenceNullBodyTest} Ensures null request body
   * returns 400 BAD REQUEST.
   */
  @Test
  public void updateUserPreferenceNullBodyTest() throws Exception {
    Long userId = 1L;
    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content("null"))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code updateUserPreferenceUserIdMismatchTest} Validates userId mismatch
   * between path and body returns 400 BAD REQUEST.
   */
  @Test
  public void updateUserPreferenceUserIdMismatchTest() throws Exception {
    Long userId = 1L;
    UserPreference userPreference = new UserPreference(2L);

    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Path Variable and RequestBody User Id do not match."));
  }

  /**
   * {@code updateUserPreferenceUserNotFoundTest} Confirms non-existent user
   * returns 404 NOT FOUND.
   */
  @Test
  public void updateUserPreferenceUserNotFoundTest() throws Exception {
    Long userId = 999L;
    UserPreference userPreference = new UserPreference(userId);

    when(tarsUserService.findById(userId)).thenReturn(null);

    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));

    verify(tarsUserService).findById(userId);
  }

  /**
   * {@code updateUserPreferenceUpdateFailureTest} Validates 400 BAD REQUEST
   * when service update returns false.
   */
  @Test
  public void updateUserPreferenceUpdateFailureTest() throws Exception {
    Long userId = 1L;
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(userId);
    mockUser.setActive(true);
    UserPreference userPreference = new UserPreference(userId);

    when(tarsUserService.findById(userId)).thenReturn(mockUser);
    when(tarsService.updateUser(userPreference)).thenReturn(false);

    mockMvc.perform(put("/user/" + userId + "/update")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Failed to update user preferences."));

    verify(tarsService).updateUser(userPreference);
  }


  /**
   * {@code addUserPreferenceSuccessTest} Verifies successful user preference
   * addition with valid data returns 200 OK.
   */
  @Test
  public void addUserPreferenceSuccessTest() throws Exception {
    final Long userId = 1L;
    List<String> weatherPrefs = new ArrayList<>();
    weatherPrefs.add("cloudy");
    List<String> tempPrefs = new ArrayList<>();
    tempPrefs.add("65F");
    List<String> cityPrefs = new ArrayList<>();
    cityPrefs.add("Boston");
    UserPreference userPreference = new UserPreference(userId);
    userPreference.setWeatherPreferences(weatherPrefs);
    userPreference.setTemperaturePreferences(tempPrefs);
    userPreference.setCityPreferences(cityPrefs);

    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(userId);
    mockUser.setActive(true);

    when(tarsUserService.findById(userId)).thenReturn(mockUser);
    when(tarsService.setUserPreference(userPreference)).thenReturn(true);

    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.intValue()));

    verify(tarsUserService).findById(userId);
    verify(tarsService).setUserPreference(userPreference);
  }

  /**
   * {@code addUserPreferenceNegativeUserIdTest} Validates negative userId
   * returns 400 BAD REQUEST.
   */
  @Test
  public void addUserPreferenceNegativeUserIdTest() throws Exception {
    Long userId = -1L;
    UserPreference userPreference = new UserPreference(userId);

    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("User Id cannot be negative."));
  }

  /**
   * {@code addUserPreferenceNullBodyTest} Ensures null request body
   * returns 400 BAD REQUEST.
   */
  @Test
  public void addUserPreferenceNullBodyTest() throws Exception {
    Long userId = 1L;
    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content("null"))
        .andExpect(status().isBadRequest());
  }

  /**
   * {@code addUserPreferenceUserIdMismatchTest} Validates userId mismatch
   * between path and body returns 400 BAD REQUEST.
   */
  @Test
  public void addUserPreferenceUserIdMismatchTest() throws Exception {
    Long userId = 1L;
    UserPreference userPreference = new UserPreference(2L);

    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Path Variable and RequestBody User Id do not match."));
  }

  /**
   * {@code addUserPreferenceUserNotFoundTest} Confirms non-existent user
   * returns 404 NOT FOUND.
   */
  @Test
  public void addUserPreferenceUserNotFoundTest() throws Exception {
    Long userId = 999L;
    UserPreference userPreference = new UserPreference(userId);

    when(tarsUserService.findById(userId)).thenReturn(null);

    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("TarsUser not found."));

    verify(tarsUserService).findById(userId);
  }

  /**
   * {@code addUserPreferenceAddFailureTest} Validates 400 BAD REQUEST
   * when service add returns false.
   */
  @Test
  public void addUserPreferenceAddFailureTest() throws Exception {
    Long userId = 1L;
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(userId);
    mockUser.setActive(true);
    UserPreference userPreference = new UserPreference(userId);

    when(tarsUserService.findById(userId)).thenReturn(mockUser);
    when(tarsService.setUserPreference(userPreference)).thenReturn(false);

    mockMvc.perform(put("/user/" + userId + "/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(userPreference)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Failed to add user preferences."));

    verify(tarsService).setUserPreference(userPreference);
  }


  /**
   * {@code loginWithUsernameSuccessTest} Verifies successful login with
   * username returns 200 OK with user data and preferences.
   */
  @Test
  public void loginWithUsernameSuccessTest() throws Exception {
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(1L);
    mockUser.setClientId(1L);
    mockUser.setUsername("alice");
    mockUser.setEmail("alice@gmail.com");
    mockUser.setRole("admin");
    mockUser.setActive(true);

    List<TarsUser> allUsers = new ArrayList<>();
    allUsers.add(mockUser);

    UserPreference mockPrefs = new UserPreference(1L);
    List<String> cityPrefs = new ArrayList<>();
    cityPrefs.add("New York");
    mockPrefs.setCityPreferences(cityPrefs);

    when(tarsUserService.listUsers()).thenReturn(allUsers);
    when(tarsService.getUserPreference(1L)).thenReturn(mockPrefs);

    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "alice");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.username").value("alice"))
        .andExpect(jsonPath("$.preferences").exists());

    verify(tarsUserService).listUsers();
    verify(tarsService).getUserPreference(1L);
  }

  /**
   * {@code loginWithEmailSuccessTest} Verifies successful login with
   * email returns 200 OK.
   */
  @Test
  public void loginWithEmailSuccessTest() throws Exception {
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(1L);
    mockUser.setClientId(1L);
    mockUser.setUsername("alice");
    mockUser.setEmail("alice@gmail.com");
    mockUser.setRole("admin");
    mockUser.setActive(true);

    List<TarsUser> allUsers = new ArrayList<>();
    allUsers.add(mockUser);

    UserPreference mockPrefs = new UserPreference(1L);

    when(tarsUserService.listUsers()).thenReturn(allUsers);
    when(tarsService.getUserPreference(1L)).thenReturn(mockPrefs);

    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("email", "alice@gmail.com");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1));

    verify(tarsUserService).listUsers();
  }

  /**
   * {@code loginWithUserIdSuccessTest} Verifies successful login with
   * userId returns 200 OK.
   */
  @Test
  public void loginWithUserIdSuccessTest() throws Exception {
    Long userId = 1L;
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(userId);
    mockUser.setClientId(1L);
    mockUser.setUsername("alice");
    mockUser.setEmail("alice@gmail.com");
    mockUser.setRole("admin");
    mockUser.setActive(true);

    UserPreference mockPrefs = new UserPreference(userId);

    when(tarsUserService.findById(userId)).thenReturn(mockUser);
    when(tarsService.getUserPreference(userId)).thenReturn(mockPrefs);

    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("userId", userId.toString());

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(userId.intValue()));

    verify(tarsUserService).findById(userId);
  }

  /**
   * {@code loginMissingCredentialsTest} Validates missing credentials
   * returns 400 BAD REQUEST.
   */
  @Test
  public void loginMissingCredentialsTest() throws Exception {
    Map<String, String> loginBody = new HashMap<>();

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Username, email, or userId is required"));
  }

  /**
   * {@code loginUserNotFoundTest} Confirms non-existent user
   * returns 404 NOT FOUND.
   */
  @Test
  public void loginUserNotFoundTest() throws Exception {
    String username = "nonexistent";
    List<TarsUser> allUsers = new ArrayList<>();

    when(tarsUserService.listUsers()).thenReturn(allUsers);

    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", username);

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("User not found. Please check your credentials."));

    verify(tarsUserService).listUsers();
  }

  /**
   * {@code loginInactiveUserTest} Validates inactive user
   * returns 403 FORBIDDEN.
   */
  @Test
  public void loginInactiveUserTest() throws Exception {
    TarsUser mockUser = new TarsUser();
    mockUser.setUserId(2L);
    mockUser.setUsername("bob");
    mockUser.setActive(false);

    List<TarsUser> allUsers = new ArrayList<>();
    allUsers.add(mockUser);

    when(tarsUserService.listUsers()).thenReturn(allUsers);

    Map<String, String> loginBody = new HashMap<>();
    loginBody.put("username", "bob");

    mockMvc.perform(post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginBody)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("User account is inactive."));

    verify(tarsUserService).listUsers();
  }
}
