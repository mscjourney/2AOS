package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import org.coms4156.tars.model.CitySummary;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientCityTest} Tests for TarsApiClient city summary methods.
 */
public class TarsApiClientCityTest extends TarsApiClientTestBase {

  /**
   * {@code getCitySummarySuccessTest} Verifies successful city summary retrieval.
   */
  @Test
  void getCitySummarySuccessTest() throws IOException, InterruptedException {
    CitySummary expected = new CitySummary();
    expected.setCity("Boston");

    testServer.createContext("/summary/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String path = exchange.getRequestURI().getPath();
        assertTrue(path.contains("Boston"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    CitySummary result = client.getCitySummary("Boston");
    assertEquals("Boston", result.getCity());
  }

  /**
   * {@code getCitySummaryWithDateRangeTest} Verifies city summary with date range.
   */
  @Test
  void getCitySummaryWithDateRangeTest() throws IOException, InterruptedException {
    CitySummary expected = new CitySummary();
    expected.setCity("New York");

    testServer.createContext("/summary/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("startDate=2025-01-01"));
        assertTrue(query.contains("endDate=2025-01-31"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    CitySummary result = client.getCitySummary("New York", "2025-01-01", "2025-01-31");
    assertEquals("New York", result.getCity());
  }

  /**
   * {@code getCitySummaryWithOnlyStartDateTest} Verifies city summary with only start date.
   */
  @Test
  void getCitySummaryWithOnlyStartDateTest() throws IOException, InterruptedException {
    CitySummary expected = new CitySummary();
    expected.setCity("Chicago");

    testServer.createContext("/summary/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String query = exchange.getRequestURI().getQuery();
        assertTrue(query != null && query.contains("startDate=2025-01-01"));
        assertFalse(query.contains("endDate"));
        String response = objectMapper.writeValueAsString(expected);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(response.getBytes());
        }
      }
    });
    testServer.start();

    CitySummary result = client.getCitySummary("Chicago", "2025-01-01", null);
    assertEquals("Chicago", result.getCity());
  }
}

