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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test for custom API key header configuration.
 */
@SpringBootTest(properties = {
  "security.apiKey.header=X-CUSTOM-Key",
  "security.enabled=true"
})
@AutoConfigureMockMvc
public class ApiKeyHeaderConfigTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String TEST_API_KEY = "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb";

  /**
   * Seeds test client data before running tests.
   *
   * @throws IOException if file operations fail
   */
  @BeforeAll
  public static void seedClients() throws IOException {
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
      Client c = new Client(2L, "custom-header-client", "custom@example.com", TEST_API_KEY);
      clients.add(c);
    }
    MAPPER.writeValue(file, clients);
  }

  @Test
  public void protectedRouteWithCustomHeaderReturns200() throws Exception {
    mockMvc.perform(get("/tarsUsers").header("X-CUSTOM-Key", TEST_API_KEY))
        .andExpect(status().isOk());
  }

  @Test
  public void protectedRouteMissingCustomHeaderReturns401() throws Exception {
    mockMvc.perform(get("/tarsUsers"))
        .andExpect(status().isUnauthorized());
  }
}
