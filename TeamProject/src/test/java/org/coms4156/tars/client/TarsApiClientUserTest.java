package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.User;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientUserTest} Tests for TarsApiClient user-related methods.
 */
public class TarsApiClientUserTest extends TarsApiClientTestBase {

  /**
   * {@code createClientUserSuccessTest} Verifies successful user creation.
   */
  @Test
  void createClientUserSuccessTest() throws IOException, InterruptedException {
    TarsUser expectedUser = new TarsUser(1L, "testuser", "user@example.com", "admin");
    expectedUser.setUserId(10L);
    expectedUser.setActive(true);

    testServer.createContext("/client/createUser", exchange -> {
      if ("POST".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedUser);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    TarsUser result = client.createClientUser(1L, "testuser", "user@example.com", "admin");
    assertEquals(10L, result.getUserId());
    assertEquals("testuser", result.getUsername());
    assertEquals("user@example.com", result.getEmail());
    assertEquals("admin", result.getRole());
  }

  /**
   * {@code addUserSuccessTest} Verifies successful user preference addition.
   */
  @Test
  void addUserSuccessTest() throws IOException, InterruptedException {
    User expectedUser = new User(1, 1);
    expectedUser.setCityPreferences(List.of("Boston", "New York"));
    expectedUser.setWeatherPreferences(List.of("sunny"));
    expectedUser.setTemperaturePreferences(List.of("70F"));

    testServer.createContext("/user/1/add", exchange -> {
      if ("PUT".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedUser);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    User user = new User(1, 1);
    user.setCityPreferences(List.of("Boston", "New York"));
    User result = client.addUser(1, user);
    assertEquals(1, result.getId());
    assertNotNull(result.getCityPreferences());
    assertEquals(2, result.getCityPreferences().size());
  }

  /**
   * {@code updateUserSuccessTest} Verifies successful user update.
   */
  @Test
  void updateUserSuccessTest() throws IOException, InterruptedException {
    User expectedUser = new User(1, 1);
    expectedUser.setCityPreferences(List.of("Boston"));
    expectedUser.setWeatherPreferences(List.of("sunny", "cloudy"));

    testServer.createContext("/user/1/update", exchange -> {
      if ("PUT".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedUser);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    User user = new User(1, 1);
    user.setCityPreferences(List.of("Boston"));
    User result = client.updateUser(1, user);
    assertEquals(1, result.getId());
    assertEquals(1, result.getCityPreferences().size());
  }

  /**
   * {@code updateUserErrorResponseTest} Verifies error handling for failed update.
   */
  @Test
  void updateUserErrorResponseTest() {
    testServer.createContext("/user/1/update", exchange -> {
      if ("PUT".equals(exchange.getRequestMethod())) {
        String response = "User not found";
        exchange.sendResponseHeaders(404, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    User user = new User(1, 1);
    assertThrows(IOException.class, () -> client.updateUser(1, user));
  }

  /**
   * {@code getUserSuccessTest} Verifies successful user retrieval.
   */
  @Test
  void getUserSuccessTest() throws IOException, InterruptedException {
    User expectedUser = new User(1, 1);
    expectedUser.setCityPreferences(List.of("Boston"));

    testServer.createContext("/user/1", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedUser);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    User result = client.getUser(1);
    assertEquals(1, result.getId());
    assertEquals(1, result.getClientId());
  }

  /**
   * {@code getUserListSuccessTest} Verifies successful user list retrieval.
   */
  @Test
  void getUserListSuccessTest() throws IOException, InterruptedException {
    List<User> expectedUsers = new ArrayList<>();
    User user1 = new User(1, 1);
    User user2 = new User(2, 1);
    expectedUsers.add(user1);
    expectedUsers.add(user2);

    testServer.createContext("/userList", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedUsers);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    List<User> result = client.getUserList();
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).getId());
    assertEquals(2, result.get(1).getId());
  }

  /**
   * {@code getUserListEmptyTest} Verifies handling of empty user list.
   */
  @Test
  void getUserListEmptyTest() throws IOException, InterruptedException {
    testServer.createContext("/userList", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String response = "[]";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    List<User> result = client.getUserList();
    assertTrue(result.isEmpty());
  }
}

