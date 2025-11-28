package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for the User Models.
 */
@SpringBootTest
public class ClientModelTest {
  public Client client;

  @BeforeEach()
  public void setUpClient() {
    client = new Client(5L, "John", "test@gmail.com", "abc123");
  }

  @Test
  public void testdefaultClient() {
    client = new Client();
    assertEquals(client.getClientId(), null);
    assertEquals(client.getName(), "");
    assertEquals(client.getEmail(), "");
    assertEquals(client.getApiKey(), "");
    assertEquals(client.getRateLimitPerMinute(), 60);
    assertEquals(client.getMaxConcurrentRequests(), 5);
  }

  @Test
  public void testSetClientId() {
    assertEquals(client.getClientId(), 5);
    client.setClientId(15L);
    assertEquals(client.getClientId(), 15);
  }

  @Test
  public void testSetName() {
    assertEquals(client.getName(), "John");
    client.setName("Alice");
    assertEquals(client.getName(), "Alice");
  }

  @Test
  public void testSetEmail() {
    assertEquals(client.getEmail(), "test@gmail.com");
    client.setEmail("different@gmail.com");
    assertEquals(client.getEmail(), "different@gmail.com");
  }

  @Test
  public void testSetApiKey() {
    assertEquals(client.getApiKey(), "abc123");
    client.setApiKey("098zyx");
    assertEquals(client.getApiKey(), "098zyx");
  }

  @Test
  public void testSetRateLimitPerMinute() {
    assertEquals(client.getRateLimitPerMinute(), 60);
    client.setRateLimitPerMinute(100);
    assertEquals(client.getRateLimitPerMinute(), 100);
  }

  @Test
  public void testSetMaxConcurrentRequests() {
    assertEquals(client.getMaxConcurrentRequests(), 5);
    client.setMaxConcurrentRequests(10);
    assertEquals(client.getMaxConcurrentRequests(), 10);
  }

  @Test
  public void testPrintClient() {
    String expected = String.format(
        "Client{clientId=%d, name='%s', email='%s', rateLimitPerMinute=%d, "
          + "maxConcurrentRequests=%d}", 5L, "John", "test@gmail.com", 60, 5);

    assertEquals(expected, client.toString());

    client = new Client();
    assertEquals(
        "Client{clientId=null, name='', email='', rateLimitPerMinute=60, maxConcurrentRequests=5}",
        client.toString()
    );
  }

  @Test
  public void testClientEquals() {
    // Object should be equal to iself
    assertTrue(client.equals(client));

    // Different Client Ids should result in false
    Client newClient = new Client();
    assertFalse(client.equals(newClient));
    assertFalse(newClient.equals(client));

    // Same Client Ids should result in true
    Client newClient2 = new Client(5L, "Bob", "", "");
    assertTrue(client.equals(newClient2));

    // Different Class should result in false
    assertFalse(client.equals("Something Else"));

    // Null objects should result in false
    assertFalse(client.equals(null));
  }

  @Test
  public void testClientHashCode() {
    Client newClient = new Client(5L, "Charlie", "", "");
    assertEquals(client.hashCode(), newClient.hashCode());

    Client emptyClient = new Client();
    assertFalse(client.hashCode() == emptyClient.hashCode());
  }
}
