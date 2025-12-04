package org.coms4156.tars.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Integration test for admin access control and rate limiting functionality.
 */
@SpringBootTest(properties = {
  "security.enabled=true",
  "security.adminApiKeys=adminkey000000000000000000000000",
  "security.apiKey.header=X-API-Key"
}, classes = org.coms4156.tars.TarsApplication.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(org.coms4156.tars.security.SecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminAndRateLimitTest {

  @Autowired private WebApplicationContext context;
  @Autowired private org.coms4156.tars.security.ApiKeyAuthFilter apiKeyAuthFilter;
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static long CLIENT_ID;
  private static String CLIENT_KEY;

  /**
   * Seeds test client for rate limiting tests.
   *
   * @throws IOException if file operations fail
   */
  @BeforeAll
  public static void setup() throws IOException {
    File f = new File(DATA_PATH);
    if (!f.exists()) {
      f.getParentFile().mkdirs();
      MAPPER.writeValue(f, new ArrayList<Client>());
    }
    List<Client> clients;
    try {
      clients = MAPPER.readValue(f, new TypeReference<List<Client>>() {});
    } catch (IOException e) {
      clients = new ArrayList<>();
    }
    if (clients.isEmpty()) {
      Client c = new Client(3L, "rl-client", "rl@example.com", "clientkey000000000000000000000000");
      c.setRateLimitPerMinute(1); // Very low for testing
      clients.add(c);
    }
    Client first = clients.get(0);
    // Ensure low rate limit for selected client
    first.setRateLimitPerMinute(1);
    CLIENT_ID = first.getClientId();
    CLIENT_KEY = first.getApiKey();
    MAPPER.writeValue(f, clients);
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
  public void nonAdminAccessClientsEndpointForbidden() throws Exception {
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/rotateKey")
            .header("X-API-Key", CLIENT_KEY))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(2)
  public void adminAccessClientsEndpointAllowed() throws Exception {
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/rotateKey")
            .header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isOk());
  }

  @Test
  @Order(3)
  public void rateLimitExceededReturns429() throws Exception {
    // Reload current key in case it was rotated in previous test
    List<Client> clients = MAPPER.readValue(new File(DATA_PATH),
        new TypeReference<List<Client>>() {});
    for (Client c : clients) {
      if (c.getClientId().equals(CLIENT_ID)) {
        CLIENT_KEY = c.getApiKey();
        break;
      }
    }
    // Ensure very low rate limit for the client
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/setRateLimit")
            .header("X-API-Key", "adminkey000000000000000000000000")
            .contentType("application/json").content("{\"limit\":1}"))
        .andExpect(status().isOk());
    // First allowed
    mockMvc.perform(get("/tarsUsers").header("X-API-Key", CLIENT_KEY))
        .andExpect(status().isOk());
    // Second within same minute should be limited (limit=1)
    mockMvc.perform(get("/tarsUsers").header("X-API-Key", CLIENT_KEY))
        .andExpect(status().isTooManyRequests());
  }
}
