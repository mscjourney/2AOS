package org.coms4156.tars.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
public class ClientsAdminAccessTest {

  @Autowired
  private MockMvc mockMvc;

  private static final String DATA_PATH = "./data/clients.json";
  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static String CLIENT_KEY;

  @BeforeAll
  public static void loadClientKey() throws Exception {
    List<Client> clients = MAPPER.readValue(new File(DATA_PATH), new TypeReference<List<Client>>() {});
    if (!clients.isEmpty()) {
      CLIENT_KEY = clients.get(0).getApiKey();
    } else {
      CLIENT_KEY = "clientkey000000000000000000000000";
    }
  }

  @Test
  public void clientsListForbiddenForClientKey() throws Exception {
    mockMvc.perform(get("/clients").header("X-API-Key", CLIENT_KEY))
        .andExpect(status().isForbidden());
  }

  @Test
  public void clientsListAllowedForAdminKey() throws Exception {
    mockMvc.perform(get("/clients").header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isOk());
  }

  @Test
  public void clientGetByIdForbiddenForClientKey() throws Exception {
    mockMvc.perform(get("/clients/1").header("X-API-Key", CLIENT_KEY))
        .andExpect(status().isForbidden());
  }

  @Test
  public void clientGetByIdAllowedForAdminKey() throws Exception {
    mockMvc.perform(get("/clients/1").header("X-API-Key", "adminkey000000000000000000000000"))
        .andExpect(status().isOk());
  }

  @Test
  public void clientCreateForbiddenForClientKey() throws Exception {
    mockMvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/client/create")
            .header("X-API-Key", CLIENT_KEY)
            .contentType("application/json")
            .content("{\"name\":\"new\",\"email\":\"new@example.com\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  public void clientCreateAllowedForAdminKey() throws Exception {
    String unique = java.util.UUID.randomUUID().toString().substring(0,8);
    mockMvc.perform(
      org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/client/create")
        .header("X-API-Key", "adminkey000000000000000000000000")
        .contentType("application/json")
        .content("{\"name\":\"newadmin-" + unique + "\",\"email\":\"newadmin-" + unique + "@example.com\"}"))
      .andExpect(status().isCreated());
  }
}
