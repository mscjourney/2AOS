package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientCountryTest} Tests for TarsApiClient country advisory methods.
 */
public class TarsApiClientCountryTest extends TarsApiClientTestBase {

  /**
   * {@code getCountryAdvisorySuccessTest} Verifies successful country advisory retrieval.
   */
  @Test
  void getCountryAdvisorySuccessTest() throws IOException, InterruptedException {
    String expectedAdvisory = "Travel advisory for France: Exercise normal precautions.";

    testServer.createContext("/country/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String path = exchange.getRequestURI().getPath();
        assertTrue(path.contains("France"));
        exchange.sendResponseHeaders(200, expectedAdvisory.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(expectedAdvisory.getBytes());
        }
      }
    });
    testServer.start();

    String result = client.getCountryAdvisory("France");
    assertEquals(expectedAdvisory, result);
  }

  /**
   * {@code getCountryAdvisoryWithSpecialCharactersTest} Verifies URL encoding for country names.
   */
  @Test
  void getCountryAdvisoryWithSpecialCharactersTest() throws IOException, InterruptedException {
    String expectedAdvisory = "Advisory for United States";

    testServer.createContext("/country/", exchange -> {
      if ("GET".equals(exchange.getRequestMethod())) {
        String path = exchange.getRequestURI().getPath();
        assertTrue(path.contains("United"));
        exchange.sendResponseHeaders(200, expectedAdvisory.length());
        try (OutputStream os = exchange.getResponseBody()) {
          os.write(expectedAdvisory.getBytes());
        }
      }
    });
    testServer.start();

    String result = client.getCountryAdvisory("United States");
    assertEquals(expectedAdvisory, result);
  }
}

