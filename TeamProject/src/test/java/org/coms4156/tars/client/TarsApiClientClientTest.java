package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientClientTest} Tests for TarsApiClient client creation methods.
 */
public class TarsApiClientClientTest extends TarsApiClientTestBase {

  /**
   * {@code createClientSuccessTest} Verifies successful client creation.
   */
  @Test
  void createClientSuccessTest() throws IOException, InterruptedException {
    Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("clientId", 1L);
    expectedResponse.put("name", "TestClient");
    expectedResponse.put("email", "test@example.com");
    expectedResponse.put("message", "Client created successfully");

    testServer.createContext("/client/create", exchange -> {
      if ("POST".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    Map<String, Object> result = client.createClient("TestClient", "test@example.com");
    // JSON deserialization may convert Long to Integer, so check value instead of type
    assertEquals(1, ((Number) result.get("clientId")).intValue());
    assertEquals("TestClient", result.get("name"));
    assertEquals("test@example.com", result.get("email"));
  }

  /**
   * {@code createClientWithSpecialCharactersTest} Verifies handling of special characters.
   */
  @Test
  void createClientWithSpecialCharactersTest() throws IOException, InterruptedException {
    Map<String, Object> expectedResponse = new HashMap<>();
    expectedResponse.put("clientId", 2L);
    expectedResponse.put("name", "Client & Co.");
    expectedResponse.put("email", "test+tag@example.com");

    testServer.createContext("/client/create", exchange -> {
      if ("POST".equals(exchange.getRequestMethod())) {
        String response = objectMapper.writeValueAsString(expectedResponse);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(201, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    Map<String, Object> result = client.createClient("Client & Co.", "test+tag@example.com");
    assertEquals("Client & Co.", result.get("name"));
  }
}

