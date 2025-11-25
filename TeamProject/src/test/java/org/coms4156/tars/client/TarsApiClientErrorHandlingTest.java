package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientErrorHandlingTest} Tests for TarsApiClient error handling.
 */
public class TarsApiClientErrorHandlingTest extends TarsApiClientTestBase {

  /**
   * {@code serverNotAvailableTest} Verifies handling when server is not available.
   */
  @Test
  void serverNotAvailableTest() {
    TarsApiClient clientToBadServer = new TarsApiClient("http://localhost:99999");
    // Connection refused or invalid port may throw IOException or IllegalArgumentException
    assertThrows(Exception.class, () -> clientToBadServer.getIndex());
  }

  /**
   * {@code invalidJsonResponseTest} Verifies handling of invalid JSON response.
   */
  @Test
  void invalidJsonResponseTest() {
    testServer.createContext("/user/1", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String response = "Invalid JSON {";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    assertThrows(IOException.class, () -> client.getUser(1));
  }
}

