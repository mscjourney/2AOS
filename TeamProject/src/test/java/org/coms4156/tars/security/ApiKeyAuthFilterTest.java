package org.coms4156.tars.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration test for API key authentication filter.
 */
@SpringBootTest
@TestPropertySource(properties = "security.enabled=true")
@AutoConfigureMockMvc(addFilters = true)
@Import(org.coms4156.tars.security.SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiKeyAuthFilterTest {

  @Autowired private WebApplicationContext context;
  @Autowired private org.coms4156.tars.security.ApiKeyAuthFilter apiKeyAuthFilter;
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String TEST_API_KEY = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 32 hex chars

  /**
   * Seeds test client for authentication tests.
   *
   * @throws IOException if file operations fail
   */
  @BeforeAll
  public static void setup() throws IOException {
    File file = new File(DATA_PATH);
    File parent = file.getParentFile();
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }

    List<Client> clients;
    if (file.exists()) {
      try {
        clients = MAPPER.readValue(file, new TypeReference<List<Client>>() {});
      } catch (IOException e) {
        clients = new ArrayList<>();
      }
    } else {
      clients = new ArrayList<>();
    }

    boolean exists = false;
    for (Client c : clients) {
      if (TEST_API_KEY.equals(c.getApiKey())) {
        exists = true;
        break;
      }
    }
    if (!exists) {
      Client c = new Client(1L, "test-client", "test@example.com", TEST_API_KEY);
      clients.add(c);
    }
    MAPPER.writeValue(file, clients);
  }

  /**
   * Sets up MockMvc with the API key auth filter before each test.
   */
  @org.junit.jupiter.api.BeforeEach
  public void setupMockMvc() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .addFilters(apiKeyAuthFilter)
        .build();
  }

  @Test
  @Order(1)
  public void protectedRouteWithoutApiKeyReturns401() throws Exception {
    mockMvc.perform(get("/tarsUsers"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(2)
  public void protectedRouteWithValidApiKeyReturns200() throws Exception {
    mockMvc.perform(get("/tarsUsers").header("X-API-Key", TEST_API_KEY))
        .andExpect(status().isOk());
  }

  @Test
  @Order(3)
  public void publicRouteBypassedReturns200() throws Exception {
    mockMvc.perform(get("/"))
        .andExpect(status().isOk());
  }

  @Test
  @Order(4)
  public void protectedRouteWithInvalidApiKeyReturns401() throws Exception {
    mockMvc.perform(get("/tarsUsers").header("X-API-Key", "deadbeefdeadbeefdeadbeefdeadbeef"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(5)
  public void staticResourceBypassDoesNotReturn401() throws Exception {
    // Expect 404 if resource doesn't exist, but not 401 Unauthorized
    mockMvc.perform(get("/static/test.js"))
        .andExpect(result -> {
          int sc = result.getResponse().getStatus();
          if (sc == 401) {
            throw new AssertionError("Expected non-401 for static path; got 401");
          }
        });
  }
}
