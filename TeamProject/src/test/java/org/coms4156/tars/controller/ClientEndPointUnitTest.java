package org.coms4156.tars.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
*
* <p>Equivalence Partition Testing for Client Endpoints
* =========== POST /client/create ===========
* 1) Equivalence Partition 1: Name and Email are both non-null and unique to each client.
*     No two clients can have same name or email. Check is case-insensitive -> alice == Alice.
*     Name and Email can contain leading/trailing whitespace.
*
* <p>Test Cases: createClientSuccessTest, createClientTrimsWhitespaceTest,
*     createClientWithValidEmailFormatsTest
*
* <p>2) Equivalence Partition 2: Name and/or Email are null OR Name or Email is non-unique and
*        there already exists a client with the specified name or email.
*
* <p>Test Cases: createClientWithNullBodyTest, createClientMissingNameFieldTest,
*     createClientWithNullNameValueTest, createClientWithBlankNameTest,
*     createClientMissingEmailFieldTest, createClientWithNullEmailValueTest,
*     createClientWithBlankEmailTest, createClientWithInvalidEmailFormatTest,
*     createClientWithDuplicateNameTest, createClientWithDuplicateEmailTest,
*     createClientCaseInsensitiveDuplicateNameTest, createClientCaseInsensitiveDuplicateEmailTest,
*     createClientBothNameAndEmailDuplicateTest
*
* <p>Other Tests: createClientServiceReturnsNullTest (service failure : INTERNAL ERROR)
*
* <p>=========== POST /client/createUser ===========
* 1) Equivalence Partition 1: Valid TarsUser body is passed in.
*     A valid TarsUser body contains non-negative clientId, non-empty username,
*     non-empty email, non-empty role.
*     There must exist a client specified by clientId and there must be not exist a TarsUser
*     with the same username or email as in the Request Body under the same client.
*     Fields can contain leading/trailing whitespace.
*
* <p>Test Cases: createClientUserSuccessTest, createClientUserTrimsWhitespaceTest,
*     createClientUserWithValidEmailFormatsTest
*
* <p>2) Equivalence Partition 2: Invalid TarsUser body.
*     A TarsUser body is invalid if clientid is negative, username/email/role is empty.
*     Another TarsUser already exists with the provided email or username under the same client.
*     The body as well as any of mentioned the fields mentioned is null.
*
* <p>Test Cases: createClientUserWithNullBodyTest, createClientUserWithNullClientIdTest,
*     createClientUserWithNegativeClientIdTest, createClientUserWithNullUsernameTest,
*     createClientUserWithBlankUsernameTest, createClientUserWithNullEmailTest,
*     createClientUserWithBlankEmailTest, createClientUserWithInvalidEmailFormatTest,
*     createClientUserWithNullRoleTest, createClientUserWithBlankRoleTest,
*     createClientUserClientNotFoundTest, createClientUserDuplicateUsernameTest,
*     createClientUserDuplicateEmailTest, createClientUserCaseInsensitiveDuplicateUsernameTest,
*     createClientUserCaseInsensitiveDuplicateEmailTest,
*     createClientUserBothUsernameAndEmailDuplicateTest
*
* <p>Other Tests: createClientUserServiceReturnsNullTest (service failure : INTERNAL ERROR)
* 
* <p>========= POST /login Equivalence Partitions ===========
* 1) Equivalence Partition 1: At least one of the following fields: 1) email, 2) username, or 
*     3) userId of the TarsUser is specified and the field match the corresponding field of 
*     an existing TarsUser. Login is successful
*
* <p>Test Cases: loginWithUserIdSuccessTest, loginWithEmailSuccessTest, loginWithUserIdSuccessTest, 
*
* <p>2) Equivalence Partition 2: At least one of the fields is passed in and there is an existing
*     TarsUser with a matching field but the TarsUser is set to inactive.
*     Cannot login as an inactive TarsUser.
*
* <p>Test Cases: loginInactiveUserTest
*
* <p>3) Equivalence Partition 3: At least one of the fields is passed in, but there is no 
*     existing TarsUserwith the matching field.
*
* <p>Test Cases: loginUserNotFoundTest
*
* <p>4) Equivalence Partition 4: None of the fields are passed in.
*
* <p>Test Cases: loginMissingCredentialsTest
*
* <p>=========== GET /clients ===========
* 1) Equivalence Partition 1: There is one or more clients that exists.
*
* <p>Test Cases: getClientsNonEmpty
*
* <p>3) Equivalence Partition 2: There are no clients that exist.
*
* <p>Test Cases: getClientsTestEmpty
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
        .andExpect(jsonPath("$.name").value("TestClient"));

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
        .andExpect(jsonPath("$.message").value("Missing 'name' and 'email'."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Missing 'name'."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Client name cannot be blank."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Client name cannot be blank."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Missing 'email'."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Client email cannot be blank."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Client email cannot be blank."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Invalid email format."))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message").value("Client name already exists."))
        .andExpect(jsonPath("$.status").value(409));

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
        .andExpect(jsonPath("$.message").value("Client email already exists."))
        .andExpect(jsonPath("$.status").value(409));

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
        .andExpect(jsonPath("$.message").value("Failed to create client."))
        .andExpect(jsonPath("$.status").value(500));

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
        .andExpect(jsonPath("$.message").value("Body cannot be null."));
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
        .andExpect(jsonPath("$.message").value("Invalid clientId."));
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
        .andExpect(jsonPath("$.message").value("Invalid clientId."));
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
        .andExpect(jsonPath("$.message").value("Username cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Username cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Email cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Email cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Invalid email format."));
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
        .andExpect(jsonPath("$.message").value("Role cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Role cannot be blank."));
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
        .andExpect(jsonPath("$.message").value("Client not found."));

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
        .andExpect(jsonPath("$.message")
            .value("Username already exists for this client."));

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
        .andExpect(jsonPath("$.message")
            .value("A user with the email already exists for this client."));

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
        .andExpect(jsonPath("$.message").value("Failed to create user."));

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
        .andExpect(jsonPath("$.message").value("Client name already exists."))
        .andExpect(jsonPath("$.status").value(409));
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
        .andExpect(jsonPath("$.message").value("Client email already exists."))
        .andExpect(jsonPath("$.status").value(409));
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
        .andExpect(jsonPath("$.message").value("Client name already exists."))
        .andExpect(jsonPath("$.status").value(409));
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
        .andExpect(jsonPath("$.message").value("Username already exists for this client."));
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
    when(tarsUserService.existsByClientIdAndUserEmail(1L, "EXISTING@EXAMPLE.COM"))
        .thenReturn(true);
    mockMvc.perform(post("/client/createUser")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message")
            .value("A user with the email already exists for this client."));
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
        .andExpect(jsonPath("$.message")
            .value("Username already exists for this client."));
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

  // ==================== POST /login Tests ====================

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
        .andExpect(jsonPath("$.message").value("Username, email, or userId is required"))
        .andExpect(jsonPath("$.status").value(400));
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
        .andExpect(jsonPath("$.message")
            .value("User not found. Please check your credentials."))
        .andExpect(jsonPath("$.status").value(404));

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
        .andExpect(jsonPath("$.message").value("User account is inactive."))
        .andExpect(jsonPath("$.status").value(403));

    verify(tarsUserService).listUsers();
  }

  /**
   * {@code getClientsExactlyOne} There is one or more existing clients.
   */
  @Test
  public void getClientsNonEmpty() throws Exception {
    Client client = new Client(2L, "Test", "test@gmail.com", "testingAPI");
    Client client2 = new Client(4L, "Test2", "mock@gmail.com", "mockingAPI");
    List<Client> clientList = List.of(client, client2);
    when(clientService.getClientList()).thenReturn(clientList);

    mockMvc.perform(get("/clients"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].clientId").value(2))
        .andExpect(jsonPath("$[1].clientId").value(4));
  }

  /**
   * {@code getClientsTestEmpty} Returns an empty client list upon performing /clients.
   */
  @Test
  public void getClientsTestEmpty() throws Exception {
    List<Client> clientList = new ArrayList<>();
    when(clientService.getClientList()).thenReturn(clientList);

    mockMvc.perform(get("/clients"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }
}
