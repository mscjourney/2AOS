package org.coms4156.tars;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.User;
import org.coms4156.tars.service.TarsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(RouteController.class)
public class ClientEndpointTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private TarsService tarsService;

  @Test
  public void testCreateClientWithValidBody() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("sunny");
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("70F");
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Boston");

    User client = new User(1, 1, weatherPreferences, temperaturePreferences, cityPreferences);

    mockMvc.perform(post("/clients")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(client)))
        .andExpect(status().isNotImplemented())
        .andExpect(content().string(containsString("createClientRoute is not yet implemented.")));
  }

  @Test
  public void testCreateClientWithNullBody() throws Exception {
    mockMvc.perform(post("/clients")
        .contentType(MediaType.APPLICATION_JSON)
        .content(""))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateClientWithEmptyJson() throws Exception {
    mockMvc.perform(post("/clients")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isNotImplemented())
        .andExpect(content().string(containsString("createClientRoute is not yet implemented.")));
  }

  @Test
  public void testAddClientUserWithValidClientId() throws Exception {
    mockMvc.perform(post("/clients/123/newUser")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotImplemented())
        .andExpect(content().string(containsString("addClientUser is not yet implemented.")));
  }

  @Test
  public void testAddClientUserWithNumericClientId() throws Exception {
    mockMvc.perform(post("/clients/456/newUser")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotImplemented())
        .andExpect(content().string(containsString("addClientUser is not yet implemented.")));
  }

  @Test
  public void testAddClientUserWithStringClientId() throws Exception {
    mockMvc.perform(post("/clients/abc123/newUser")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotImplemented())
        .andExpect(content().string(containsString("addClientUser is not yet implemented.")));
  }
}

