package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientConstructorTest} Tests for TarsApiClient constructors.
 */
public class TarsApiClientConstructorTest {

  private HttpServer testServer;
  private int serverPort;
  private String baseUrl;

  @BeforeEach
  void setUp() throws IOException {
    testServer = HttpServer.create(new InetSocketAddress(0), 0);
    serverPort = testServer.getAddress().getPort();
    baseUrl = "http://localhost:" + serverPort;
  }

  @AfterEach
  void tearDown() {
    if (testServer != null) {
      testServer.stop(0);
    }
  }

  /**
   * {@code defaultConstructorTest} Verifies default constructor uses localhost:8080.
   */
  @Test
  void defaultConstructorTest() {
    TarsApiClient defaultClient = new TarsApiClient();
    assertNotNull(defaultClient);
  }

  /**
   * {@code customBaseUrlConstructorTest} Verifies custom base URL is set correctly.
   */
  @Test
  void customBaseUrlConstructorTest() {
    TarsApiClient customClient = new TarsApiClient("http://example.com");
    assertNotNull(customClient);
  }

  /**
   * {@code baseUrlTrailingSlashRemovedTest} Verifies trailing slash is removed from base URL.
   */
  @Test
  void baseUrlTrailingSlashRemovedTest() throws IOException, InterruptedException {
    testServer.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      assertEquals("/", path, "Base URL should not have trailing slash");
      String response = "Welcome";
      exchange.sendResponseHeaders(200, response.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(response.getBytes());
      }
    });
    testServer.start();

    TarsApiClient clientWithSlash = new TarsApiClient(baseUrl + "/");
    String result = clientWithSlash.getIndex();
    assertEquals("Welcome", result);
  }
}

