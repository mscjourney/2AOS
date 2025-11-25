package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import org.coms4156.tars.model.CrimeSummary;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientCrimeTest} Tests for TarsApiClient crime summary methods.
 */
public class TarsApiClientCrimeTest extends TarsApiClientTestBase {

  /**
   * {@code getCrimeSummarySuccessTest} Verifies successful crime summary retrieval.
   */
  @Test
  void getCrimeSummarySuccessTest() throws IOException, InterruptedException {
    CrimeSummary expected = new CrimeSummary("NC", "10", "2025", "Crime summary message");

    testServer.createContext("/crime/summary", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("state=NC"));
        assertTrue(query.contains("offense=V"));
        assertTrue(query.contains("month=10"));
        assertTrue(query.contains("year=2025"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    CrimeSummary result = client.getCrimeSummary("NC", "V", "10", "2025");
    assertEquals("NC", result.getState());
    assertEquals("10", result.getMonth());
    assertEquals("2025", result.getYear());
    assertEquals("Crime summary message", result.getMessage());
  }
}

