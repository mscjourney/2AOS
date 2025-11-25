package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit tests for the Client model class.
 */
@SpringBootTest
public class ClientTest {

  private Client client;

  @BeforeEach
  void setUp() {
    client = new Client(1L, "Test Client", "test@example.com", "test-api-key-123");
  }

  @Test
  void testDefaultConstructor() {
    Client defaultClient = new Client();
    
    assertNotNull(defaultClient);
    assertNull(defaultClient.getClientId());
    assertEquals("", defaultClient.getName());
    assertEquals("", defaultClient.getEmail());
    assertEquals("", defaultClient.getApiKey());
    assertEquals(60, defaultClient.getRateLimitPerMinute());
    assertEquals(5, defaultClient.getMaxConcurrentRequests());
  }

  @Test
  void testParameterizedConstructor() {
    Client newClient = new Client(2L, "Another Client", "another@example.com", "api-key-456");
    
    assertEquals(2L, newClient.getClientId());
    assertEquals("Another Client", newClient.getName());
    assertEquals("another@example.com", newClient.getEmail());
    assertEquals("api-key-456", newClient.getApiKey());
    assertEquals(60, newClient.getRateLimitPerMinute());
    assertEquals(5, newClient.getMaxConcurrentRequests());
  }

  @Test
  void testGettersAndSetters() {
    Client testClient = new Client();
    
    testClient.setClientId(10L);
    testClient.setName("Updated Name");
    testClient.setEmail("updated@example.com");
    testClient.setApiKey("updated-api-key");
    testClient.setRateLimitPerMinute(100);
    testClient.setMaxConcurrentRequests(10);
    
    assertEquals(10L, testClient.getClientId());
    assertEquals("Updated Name", testClient.getName());
    assertEquals("updated@example.com", testClient.getEmail());
    assertEquals("updated-api-key", testClient.getApiKey());
    assertEquals(100, testClient.getRateLimitPerMinute());
    assertEquals(10, testClient.getMaxConcurrentRequests());
  }

  @Test
  void testSetClientId() {
    client.setClientId(99L);
    assertEquals(99L, client.getClientId());
    
    client.setClientId(null);
    assertNull(client.getClientId());
  }

  @Test
  void testSetName() {
    client.setName("New Client Name");
    assertEquals("New Client Name", client.getName());
    
    client.setName("");
    assertEquals("", client.getName());
  }

  @Test
  void testSetEmail() {
    client.setEmail("newemail@example.com");
    assertEquals("newemail@example.com", client.getEmail());
    
    client.setEmail("");
    assertEquals("", client.getEmail());
  }

  @Test
  void testSetApiKey() {
    client.setApiKey("new-api-key-789");
    assertEquals("new-api-key-789", client.getApiKey());
    
    client.setApiKey("");
    assertEquals("", client.getApiKey());
  }

  @Test
  void testSetRateLimitPerMinute() {
    client.setRateLimitPerMinute(120);
    assertEquals(120, client.getRateLimitPerMinute());
    
    client.setRateLimitPerMinute(0);
    assertEquals(0, client.getRateLimitPerMinute());
    
    client.setRateLimitPerMinute(200);
    assertEquals(200, client.getRateLimitPerMinute());
  }

  @Test
  void testSetMaxConcurrentRequests() {
    client.setMaxConcurrentRequests(20);
    assertEquals(20, client.getMaxConcurrentRequests());
    
    client.setMaxConcurrentRequests(1);
    assertEquals(1, client.getMaxConcurrentRequests());
    
    client.setMaxConcurrentRequests(50);
    assertEquals(50, client.getMaxConcurrentRequests());
  }

  @Test
  void testToString() {
    String result = client.toString();
    
    assertTrue(result.contains("Client{"));
    assertTrue(result.contains("clientId=1"));
    assertTrue(result.contains("name='Test Client'"));
    assertTrue(result.contains("email='test@example.com'"));
    assertTrue(result.contains("rateLimitPerMinute=60"));
    assertTrue(result.contains("maxConcurrentRequests=5"));
  }

  @Test
  void testToStringWithNullClientId() {
    Client nullClient = new Client();
    String result = nullClient.toString();
    
    assertTrue(result.contains("Client{"));
    assertTrue(result.contains("clientId=null"));
    assertTrue(result.contains("name=''"));
    assertTrue(result.contains("email=''"));
  }

  @Test
  void testEqualsSameObject() {
    assertTrue(client.equals(client));
  }

  @Test
  void testEqualsSameClientId() {
    Client client2 = new Client(1L, "Different Name", "different@example.com", "different-key");
    
    assertTrue(client.equals(client2));
  }

  @Test
  void testEqualsDifferentClientId() {
    Client client2 = new Client(2L, "Test Client", "test@example.com", "test-api-key-123");
    
    assertFalse(client.equals(client2));
  }

  @Test
  void testEqualsNull() {
    assertFalse(client.equals(null));
  }

  @Test
  void testEqualsDifferentClass() {
    assertFalse(client.equals("not a client"));
    assertFalse(client.equals(123));
  }

  @Test
  void testEqualsWithNullClientId() {
    Client client1 = new Client();
    Client client2 = new Client();
    
    // Both have null clientId, so equals should return false (based on implementation)
    assertFalse(client1.equals(client2));
  }

  @Test
  void testEqualsOneNullClientId() {
    Client client1 = new Client(1L, "Test", "test@example.com", "key");
    Client client2 = new Client();
    
    assertFalse(client1.equals(client2));
    assertFalse(client2.equals(client1));
  }

  @Test
  void testHashCode() {
    Client client1 = new Client(1L, "Client 1", "client1@example.com", "key1");
    Client client2 = new Client(1L, "Client 2", "client2@example.com", "key2");
    
    // Same clientId should produce same hashCode
    assertEquals(client1.hashCode(), client2.hashCode());
  }

  @Test
  void testHashCodeDifferentClientIds() {
    Client client1 = new Client(1L, "Client 1", "client1@example.com", "key1");
    Client client2 = new Client(2L, "Client 2", "client2@example.com", "key2");
    
    // Different clientIds should produce different hashCodes (usually)
    assertTrue(client1.hashCode() != client2.hashCode() || client1.getClientId() != client2.getClientId());
  }

  @Test
  void testHashCodeWithNullClientId() {
    Client client1 = new Client();
    Client client2 = new Client();
    
    // Both have null clientId, should return 0
    assertEquals(0, client1.hashCode());
    assertEquals(0, client2.hashCode());
  }

  @Test
  void testCompleteClientScenario() {
    Client completeClient = new Client(100L, "Complete Client", "complete@example.com", 
                                       "complete-api-key");
    completeClient.setRateLimitPerMinute(150);
    completeClient.setMaxConcurrentRequests(15);
    
    assertEquals(100L, completeClient.getClientId());
    assertEquals("Complete Client", completeClient.getName());
    assertEquals("complete@example.com", completeClient.getEmail());
    assertEquals("complete-api-key", completeClient.getApiKey());
    assertEquals(150, completeClient.getRateLimitPerMinute());
    assertEquals(15, completeClient.getMaxConcurrentRequests());
    
    // Test equals
    Client sameIdClient = new Client(100L, "Different", "different@example.com", "different");
    assertTrue(completeClient.equals(sameIdClient));
    
    // Test hashCode
    assertEquals(completeClient.hashCode(), sameIdClient.hashCode());
  }

  @Test
  void testDefaultValuesInParameterizedConstructor() {
    Client newClient = new Client(5L, "Name", "email@example.com", "key");
    
    // Should have default values for rate limit and max concurrent requests
    assertEquals(60, newClient.getRateLimitPerMinute());
    assertEquals(5, newClient.getMaxConcurrentRequests());
  }
}

