package org.coms4156.tars.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
  "security.adminApiKeys=adminkey000000000000000000000000",
  "security.apiKey.header=X-API-Key",
  "security.enabled=true"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientKeyRotationTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  // no longer needed; rotate is admin-only
  // private static String TEST_API_KEY;
  private static long TEST_CLIENT_ID;

  @BeforeAll
  public static void ensureClient() throws IOException {
    File file = new File(DATA_PATH);
    if (!file.exists()) {
      file.getParentFile().mkdirs();
      MAPPER.writeValue(file, new ArrayList<Client>());
    }
    List<Client> clients;
    try {
      clients = MAPPER.readValue(file, new TypeReference<List<Client>>() {});
    } catch (IOException e) {
      clients = new ArrayList<>();
    }
    if (clients.isEmpty()) {
      Client c = new Client(1L, "rotation-client", "rotation@example.com", "cccccccccccccccccccccccccccccccc");
      clients.add(c);
    }
    Client first = clients.get(0);
    // client key not used for admin-only rotation
    TEST_CLIENT_ID = first.getClientId() == null ? 1L : first.getClientId();
    MAPPER.writeValue(file, clients);
  }

  @Test
  @Order(1)
  public void rotateWithoutApiKeyReturns401() throws Exception {
    mockMvc.perform(post("/clients/" + TEST_CLIENT_ID + "/rotateKey"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @Order(2)
  public void rotateWithAdminKeyReturnsNewKey() throws Exception {
    // Admin-only endpoint: use configured admin key
    mockMvc.perform(post("/clients/" + TEST_CLIENT_ID + "/rotateKey").header("X-API-Key", "adminkey000000000000000000000000"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.apiKey").exists())
      .andExpect(jsonPath("$.apiKey").isString())
      .andExpect(jsonPath("$.clientId").value(String.valueOf(TEST_CLIENT_ID)));
  }
}
