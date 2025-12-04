package org.coms4156.tars.security;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for client rate limit admin endpoint.
 */
@SpringBootTest(properties = {
    "security.adminApiKeys=adminkey000000000000000000000000",
  "security.apiKey.header=X-API-Key",
  "security.enabled=true"
})
@AutoConfigureMockMvc
public class ClientRateLimitAdminTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static long CLIENT_ID;
  private static String CLIENT_KEY;

  /**
   * Seeds test client for rate limit tests.
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
    List<Client> clients = MAPPER.readValue(f, new TypeReference<List<Client>>() {});
    if (clients.isEmpty()) {
      Client c = new Client(5L, "limit-client", "limit@example.com",
          "limitkey000000000000000000000000");
      clients.add(c);
      MAPPER.writeValue(f, clients);
    }
    Client first = clients.get(0);
    CLIENT_ID = first.getClientId();
    CLIENT_KEY = first.getApiKey();
  }

  @Test
  public void clientKeyForbiddenToSetRateLimit() throws Exception {
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/setRateLimit")
        .header("X-API-Key", CLIENT_KEY)
        .contentType("application/json")
        .content("{\"limit\":10}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void adminKeyCanSetRateLimit() throws Exception {
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/setRateLimit")
        .header("X-API-Key", "adminkey000000000000000000000000")
        .contentType("application/json")
        .content("{\"limit\":25}"))
        .andExpect(status().isOk());
  }

  @Test
  public void invalidLimitReturns400() throws Exception {
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/setRateLimit")
        .header("X-API-Key", "adminkey000000000000000000000000")
        .contentType("application/json")
        .content("{\"limit\":0}"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(post("/clients/" + CLIENT_ID + "/setRateLimit")
        .header("X-API-Key", "adminkey000000000000000000000000")
        .contentType("application/json")
        .content("{\"limit\":\"abc\"}"))
        .andExpect(status().isBadRequest());
  }
}
