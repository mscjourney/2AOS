package org.coms4156.tars.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for TarsApiClient tests.
 * Provides common setup and teardown for HTTP test server.
 */
public abstract class TarsApiClientTestBase {

  protected HttpServer testServer;
  protected TarsApiClient client;
  protected ObjectMapper objectMapper;
  protected int serverPort;
  protected String baseUrl;

  /**
   * Sets up a test HTTP server before each test.
   */
  @BeforeEach
  void setUp() throws IOException {
    testServer = HttpServer.create(new InetSocketAddress(0), 0);
    serverPort = testServer.getAddress().getPort();
    baseUrl = "http://localhost:" + serverPort;
    client = new TarsApiClient(baseUrl);
    objectMapper = new ObjectMapper();
  }

  /**
   * Stops the test server after each test.
   */
  @AfterEach
  void tearDown() {
    if (testServer != null) {
      testServer.stop(0);
    }
  }
}

