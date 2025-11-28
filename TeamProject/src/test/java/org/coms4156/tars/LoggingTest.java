package org.coms4156.tars;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.coms4156.tars.controller.RouteController;
import org.coms4156.tars.model.UserPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * This class contains unit tests for logging functionality in the application.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class LoggingTest {

  @Autowired
  private MockMvc mockMvc;

  private ListAppender<ILoggingEvent> listAppender;
  private Logger logger;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(RouteController.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
  }

  @Test
  public void testIndexEndpointLogging() throws Exception {
    listAppender.list.clear();

    mockMvc.perform(get("/"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have at least one log event", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundIndexLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("GET /index invoked"));
    assertThat("Should log GET /index invoked", foundIndexLog, org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testsetUserPreferenceEndpointLogging() throws Exception {
    listAppender.list.clear();

    final ObjectMapper mapper = new ObjectMapper();
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("sunny");
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("70F");
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Boston");

    UserPreference newUser = new UserPreference(1L, weatherPreferences, 
        temperaturePreferences, cityPreferences);

    mockMvc.perform(put("/setPreference/1")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(newUser)))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 400 && status != 404) {
            throw new AssertionError("Unexpected status: " + status);
          }
        });

    mockMvc.perform(put("/clearPreference/1"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("User Preference cleared successfully.")));

    // Verify logging occurred
    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundPutLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("PUT /setPreference/") && msg.contains("invoked"));
    assertThat("Should log PUT /setPreference/{} invoked", foundPutLog, 
        org.hamcrest.Matchers.is(true));

    boolean foundRemoveLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("PUT /clearPreference/") && msg.contains("invoked"));
    assertThat("Shold log PUT /clearPreference/{} invoked", foundRemoveLog, 
        org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testGetUserPreferenceEndpointLogging() throws Exception {
    listAppender.list.clear();

    mockMvc.perform(get("/retrievePreference/1"))
        .andExpect(result -> {
          int status = result.getResponse().getStatus();
          if (status != 200 && status != 400 && status != 404) {
            throw new AssertionError("Unexpected status: " + status);
          }
        });

    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundGetUserLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("GET /retrievePreference/") && msg.contains("invoked"));
    assertThat("Should log GET /retrievePreference/{} invoked", foundGetUserLog, 
        org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testGetUserPreferenceNotFoundWarningLogging() throws Exception {
    listAppender.list.clear();

    mockMvc.perform(get("/retrievePreference/99999"))
        .andExpect(status().isNotFound());

    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundWarningLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("TarsUser with id=") && msg.contains("does not exist."));
    assertThat("Should log warning for TarsUser not found", foundWarningLog, 
        org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testWeatherRecommendationLogging() throws Exception {
    listAppender.list.clear();

    mockMvc.perform(get("/recommendation/weather/")
        .param("city", "New York")
        .param("days", "7"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundRecommendationLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("GET /recommendation/weather") 
            && msg.contains("city=") && msg.contains("days="));
    assertThat("Should log weather recommendation request", foundRecommendationLog, 
        org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testWeatherAlertLogging() throws Exception {
    listAppender.list.clear();

    mockMvc.perform(get("/alert/weather")
        .param("city", "Boston"))
        .andExpect(status().isOk());

    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundAlertLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("GET /alert/weather") && msg.contains("city="));
    assertThat("Should log weather alert request", foundAlertLog, org.hamcrest.Matchers.is(true));
  }

  @Test
  public void testGetCountryAdvisoryLogging() throws Exception {
    listAppender.list.clear();
    
    mockMvc.perform(get("/country/Earth"))
        .andExpect(status().isNotFound());
    
    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundInvokeLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("GET /country/Earth invoked"));
    assertThat("Should log invoke message", foundInvokeLog, org.hamcrest.Matchers.is(true));

    boolean foundErrorLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("No advisory found for country=Earth"));
    assertThat("Should log no advisory found", foundErrorLog, org.hamcrest.Matchers.is(true));  
  }
  /*
   * {@code testClientEndpointLogging} Tests logging for: Remove this test.
  @Test
  public void testClientEndpointLogging() throws Exception {
    listAppender.list.clear();

    ObjectMapper mapper = new ObjectMapper();
    User client = new User(1, 1, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    mockMvc.perform(post("/clients")
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsString(client)))
        .andExpect(status().isNotImplemented());

    // Verify logging occurred
    List<ILoggingEvent> logEvents = listAppender.list;
    assertThat("Should have log events", logEvents.size(), greaterThan(0));

    List<String> logMessages = logEvents.stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());

    boolean foundClientLog = logMessages.stream()
        .anyMatch(msg -> msg.contains("POST /clients invoked") 
            && msg.contains("not implemented"));
    assertThat("Should log POST /clients invoked", foundClientLog, org.hamcrest.Matchers.is(true));
  }*/
}

