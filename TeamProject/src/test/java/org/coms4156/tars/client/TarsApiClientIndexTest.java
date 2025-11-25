package org.coms4156.tars.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@code TarsApiClientIndexTest} Tests for TarsApiClient getIndex() method.
 */
public class TarsApiClientIndexTest extends TarsApiClientTestBase {

  /**
   * {@code getIndexSuccessTest} Verifies successful retrieval of index message.
   */
  @Test
  void getIndexSuccessTest() throws IOException, InterruptedException {
    String expectedMessage = "Welcome to TARS API";
    testServer.createContext("/", exchange -> {
      String response = expectedMessage;
      exchange.sendResponseHeaders(200, response.length());
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(response.getBytes());
      }
    });
    testServer.start();

    String result = client.getIndex();
    assertEquals(expectedMessage, result);
  }

  /**
   * {@code getIndexEmptyResponseTest} Verifies handling of empty response.
   */
  @Test
  void getIndexEmptyResponseTest() throws IOException, InterruptedException {
    testServer.createContext("/", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      exchange.close();
    });
    testServer.start();

    String result = client.getIndex();
    assertEquals("", result);
  }
}

