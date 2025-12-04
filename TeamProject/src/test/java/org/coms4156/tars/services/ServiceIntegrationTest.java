package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for service classes
 * and shared data scenarios.
 *
 * <p>These tests show:
 * 1. ClientService and TarsUserService integration (clientId relationships)
 * 2. TarsUserService and TarsService integration (userId relationships)
 * 3. Data persistence and consistency across services
 * 4. Shared data file scenarios
 * 5. Cross-service query operations
 */
@SpringBootTest
public class ServiceIntegrationTest {

  private ClientService clientService;
  private TarsUserService tarsUserService;
  private TarsService tarsService;
  
  private Path tempClientFile;
  private Path tempUserFile;
  private Path tempPreferenceFile;

  @BeforeEach
  void setUp() throws IOException {
    tempClientFile = Files.createTempFile("test-clients-integration", ".json");
    tempUserFile = Files.createTempFile("test-users-integration", ".json");
    tempPreferenceFile = Files.createTempFile("test-preferences-integration", ".json");

    clientService = new ClientService(tempClientFile.toString(), null);
    tarsUserService = new TarsUserService(tempUserFile.toString(), null);
    tarsService = new TarsService(tempPreferenceFile.toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    if (tempClientFile != null && Files.exists(tempClientFile)) {
      Files.deleteIfExists(tempClientFile);
    }
    if (tempUserFile != null && Files.exists(tempUserFile)) {
      Files.deleteIfExists(tempUserFile);
    }
    if (tempPreferenceFile != null && Files.exists(tempPreferenceFile)) {
      Files.deleteIfExists(tempPreferenceFile);
    }
  }

  /**
   * Integration test: Create client, then create user for that client.
   * Verifies clientId relationship between ClientService and TarsUserService.
   */
  @Test
  public void testCreateClientThenCreateUserForClient() {
    // Create a client
    Client client = clientService.createClient("IntegrationTestClient", "integration@test.com");
    assertNotNull(client, "Client should be created");
    Long clientId = client.getClientId();
    assertNotNull(clientId, "Client should have an ID");

    // Create a user for that client
    TarsUser user = tarsUserService.createUser(
        clientId,
        "integration_user",
        "user@integration.com",
        "user"
    );
    assertNotNull(user, "User should be created");
    assertEquals(clientId, user.getClientId(), "User should reference the created client");
    assertNotNull(user.getUserId(), "User should have an ID");
  }

  /**
   * Integration test: Create multiple users for same client.
   * Verifies clientId relationship and listUsersByClientId functionality.
   */
  @Test
  public void testCreateMultipleUsersForSameClient() {
    // Create a client
    Client client = clientService.createClient("MultiUserClient", "multi@test.com");
    Long clientId = client.getClientId();

    // Create multiple users for the same client
    TarsUser user1 = tarsUserService.createUser(clientId, "user1", "user1@test.com", "user");
    TarsUser user2 = tarsUserService.createUser(clientId, "user2", "user2@test.com", "admin");
    TarsUser user3 = tarsUserService.createUser(clientId, "user3", "user3@test.com", "user");

    assertNotNull(user1);
    assertNotNull(user2);
    assertNotNull(user3);

    // Verify all users belong to the same client
    assertEquals(clientId, user1.getClientId());
    assertEquals(clientId, user2.getClientId());
    assertEquals(clientId, user3.getClientId());

    // Verify listUsersByClientId returns all users for that client
    List<TarsUser> usersForClient = tarsUserService.listUsersByClientId(clientId);
    assertEquals(3, usersForClient.size(), "Should find 3 users for the client");
    assertTrue(usersForClient.stream().anyMatch(u -> u.getUserId().equals(user1.getUserId())));
    assertTrue(usersForClient.stream().anyMatch(u -> u.getUserId().equals(user2.getUserId())));
    assertTrue(usersForClient.stream().anyMatch(u -> u.getUserId().equals(user3.getUserId())));
  }

  /**
   * Integration test: Create user, then set preferences for that user.
   * Verifies userId relationship between TarsUserService and TarsService.
   */
  @Test
  public void testCreateUserThenSetPreferences() {
    // Create a client first
    Client client = clientService.createClient("PreferenceClient", "pref@test.com");
    Long clientId = client.getClientId();

    // Create a user
    TarsUser user = tarsUserService.createUser(
        clientId,
        "pref_user",
        "pref@user.com",
        "user"
    );
    Long userId = user.getUserId();
    assertNotNull(userId, "User should have an ID");

    // Set preferences for that user
    UserPreference preference = new UserPreference(
        userId,
        List.of("sunny", "cloudy"),
        List.of("20", "25"),
        List.of("New York", "Boston")
    );
    boolean set = tarsService.setUserPreference(preference);
    assertTrue(set, "Preferences should be set successfully");

    // Verify preferences can be retrieved
    UserPreference retrieved = tarsService.getUserPreference(userId);
    assertNotNull(retrieved, "Preferences should be retrievable");
    assertEquals(userId, retrieved.getId(), "Preference ID should match user ID");
    assertEquals(2, retrieved.getWeatherPreferences().size());
    assertEquals(2, retrieved.getCityPreferences().size());
  }

  /**
   * Integration test: Full workflow - Create client, user, and preferences.
   * Verifies complete data flow across all three services.
   */
  @Test
  public void testCompleteWorkflowClientUserPreferences() {
    // Step 1: Create client
    Client client = clientService.createClient("FullWorkflowClient", "workflow@test.com");
    Long clientId = client.getClientId();
    assertNotNull(clientId);

    // Step 2: Create user for client
    TarsUser user = tarsUserService.createUser(
        clientId,
        "workflow_user",
        "workflow@user.com",
        "admin"
    );
    Long userId = user.getUserId();
    assertNotNull(userId);
    assertEquals(clientId, user.getClientId());

    // Step 3: Set user preferences
    UserPreference preference = new UserPreference(
        userId,
        List.of("rainy"),
        List.of("15"),
        List.of("Seattle")
    );
    boolean set = tarsService.setUserPreference(preference);
    assertTrue(set);

    // Step 4: Verify all relationships
    Client retrievedClient = clientService.getClient(clientId.intValue());
    assertNotNull(retrievedClient);
    assertEquals("FullWorkflowClient", retrievedClient.getName());

    TarsUser retrievedUser = tarsUserService.findById(userId);
    assertNotNull(retrievedUser);
    assertEquals(clientId, retrievedUser.getClientId());

    UserPreference retrievedPref = tarsService.getUserPreference(userId);
    assertNotNull(retrievedPref);
    assertEquals(userId, retrievedPref.getId());
    assertTrue(retrievedPref.getCityPreferences().contains("Seattle"));
  }

  /**
   * Integration test: Verify data persistence across service instances.
   * Tests that data written by one service instance is readable by another.
   */
  @Test
  public void testDataPersistenceAcrossServiceInstances() throws IOException {
    // Create client with first service instance
    Client client = clientService.createClient("PersistenceClient", "persist@test.com");
    Long clientId = client.getClientId();

    // Create user with first service instance
    TarsUser user = tarsUserService.createUser(
        clientId,
        "persist_user",
        "persist@user.com",
        "user"
    );
    Long userId = user.getUserId();

    // Set preferences with first service instance
    UserPreference preference = new UserPreference(
        userId,
        List.of("snowy"),
        List.of("0"),
        List.of("Alaska")
    );
    tarsService.setUserPreference(preference);

    // Create new service instances pointing to same files
    ClientService newClientService = new ClientService(tempClientFile.toString(), null);
    TarsUserService newUserService = new TarsUserService(tempUserFile.toString(), null);
    final TarsService newPreferenceService = new TarsService(tempPreferenceFile.toString());

    // Verify data persists
    Client persistedClient = newClientService.getClient(clientId.intValue());
    assertNotNull(persistedClient);
    assertEquals("PersistenceClient", persistedClient.getName());

    TarsUser persistedUser = newUserService.findById(userId);
    assertNotNull(persistedUser);
    assertEquals("persist_user", persistedUser.getUsername());

    UserPreference persistedPref = newPreferenceService.getUserPreference(userId);
    assertNotNull(persistedPref);
    assertTrue(persistedPref.getCityPreferences().contains("Alaska"));
  }

  /**
   * Integration test: Verify users from different clients are isolated.
   * Tests clientId-based data separation.
   */
  @Test
  public void testUsersFromDifferentClientsAreIsolated() {
    // Create two clients
    Client client1 = clientService.createClient("Client1", "client1@test.com");
    Client client2 = clientService.createClient("Client2", "client2@test.com");
    Long clientId1 = client1.getClientId();
    Long clientId2 = client2.getClientId();

    // Create users with same username but different clients
    TarsUser user1 = tarsUserService.createUser(
        clientId1, "same_username", "user1@test.com", "user");
    TarsUser user2 = tarsUserService.createUser(
        clientId2, "same_username", "user2@test.com", "user");

    assertNotNull(user1);
    assertNotNull(user2);
    assertEquals("same_username", user1.getUsername());
    assertEquals("same_username", user2.getUsername());
    assertFalse(user1.getUserId().equals(user2.getUserId()), "Users should have different IDs");

    // Verify listUsersByClientId returns correct users
    List<TarsUser> users1 = tarsUserService.listUsersByClientId(clientId1);
    List<TarsUser> users2 = tarsUserService.listUsersByClientId(clientId2);

    assertEquals(1, users1.size());
    assertEquals(1, users2.size());
    assertEquals(user1.getUserId(), users1.get(0).getUserId());
    assertEquals(user2.getUserId(), users2.get(0).getUserId());
  }

  /**
   * Integration test: Update user preferences and verify changes persist.
   * Tests TarsService update functionality with existing user.
   */
  @Test
  public void testUpdateUserPreferences() {
    // Create client and user
    Client client = clientService.createClient("UpdateClient", "update@test.com");
    TarsUser user = tarsUserService.createUser(
        client.getClientId(),
        "update_user",
        "update@user.com",
        "user"
    );
    Long userId = user.getUserId();

    // Set initial preferences
    UserPreference initial = new UserPreference(
        userId,
        List.of("sunny"),
        List.of("20"),
        List.of("Miami")
    );
    tarsService.setUserPreference(initial);

    // Update preferences
    UserPreference updated = new UserPreference(
        userId,
        List.of("rainy", "cloudy"),
        List.of("15", "18"),
        List.of("Seattle", "Portland")
    );
    boolean updatedResult = tarsService.setUserPreference(updated);
    assertTrue(updatedResult, "Preferences should be updated");

    // Verify updated preferences
    UserPreference retrieved = tarsService.getUserPreference(userId);
    assertEquals(2, retrieved.getWeatherPreferences().size());
    assertEquals(2, retrieved.getCityPreferences().size());
    assertTrue(retrieved.getCityPreferences().contains("Seattle"));
    assertFalse(retrieved.getCityPreferences().contains("Miami"));
  }

  /**
   * Integration test: Delete user and verify preferences can still exist.
   * Tests data independence between TarsUserService and TarsService.
   */
  @Test
  public void testDeleteUserPreferencesRemain() {
    // Create client and user
    Client client = clientService.createClient("DeleteClient", "delete@test.com");
    TarsUser user = tarsUserService.createUser(
        client.getClientId(),
        "delete_user",
        "delete@user.com",
        "user"
    );
    Long userId = user.getUserId();

    // Set preferences
    UserPreference preference = new UserPreference(
        userId,
        List.of("sunny"),
        List.of("25"),
        List.of("Phoenix")
    );
    tarsService.setUserPreference(preference);

    // Delete user
    TarsUser deleted = tarsUserService.deleteUser(userId);
    assertNotNull(deleted, "Deleted user should be returned");
    assertNull(tarsUserService.findById(userId), "User should no longer exist");

    // Preferences should still exist (services are independent)
    UserPreference remaining = tarsService.getUserPreference(userId);
    assertNotNull(remaining, "Preferences should still exist after user deletion");
    assertEquals(userId, remaining.getId());
  }

  /**
   * Integration test: Verify null clientId returns empty list in listUsersByClientId.
   * Tests boundary condition across service interface.
   */
  @Test
  public void testListUsersByClientIdWithNull() {
    // Create some users
    Client client = clientService.createClient("NullTestClient", "null@test.com");
    tarsUserService.createUser(client.getClientId(), "user1", "user1@test.com", "user");
    tarsUserService.createUser(client.getClientId(), "user2", "user2@test.com", "user");

    // Test null clientId
    List<TarsUser> result = tarsUserService.listUsersByClientId(null);
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Null clientId should return empty list");
  }

  /**
   * Integration test: Verify user existence checks work across service boundaries.
   * Tests existsByClientIdAndUsername and existsByClientIdAndUserEmail integration.
   */
  @Test
  public void testUserExistenceChecksAcrossServices() {
    // Create client
    Client client = clientService.createClient("ExistenceClient", "exist@test.com");
    Long clientId = client.getClientId();

    // Create user
    TarsUser user = tarsUserService.createUser(
        clientId,
        "exist_user",
        "exist@user.com",
        "user"
    );

    // Verify existence checks
    assertTrue(
        tarsUserService.existsByClientIdAndUsername(clientId, "exist_user"),
        "Username should exist for client"
    );
    assertTrue(
        tarsUserService.existsByClientIdAndUserEmail(clientId, "exist@user.com"),
        "Email should exist for client"
    );

    // Verify case-insensitive checks
    assertTrue(
        tarsUserService.existsByClientIdAndUsername(clientId, "EXIST_USER"),
        "Username check should be case-insensitive"
    );
    assertTrue(
        tarsUserService.existsByClientIdAndUserEmail(clientId, "EXIST@USER.COM"),
        "Email check should be case-insensitive"
    );

    // Verify non-existent values
    assertFalse(
        tarsUserService.existsByClientIdAndUsername(clientId, "nonexistent"),
        "Non-existent username should return false"
    );
    assertFalse(
        tarsUserService.existsByClientIdAndUserEmail(clientId, "nonexistent@test.com"),
        "Non-existent email should return false"
    );
  }

  /**
   * Integration test: Verify multiple clients with multiple users each.
   * Tests complex multi-client, multi-user scenario.
   */
  @Test
  public void testMultipleClientsMultipleUsers() {
    // Create multiple clients
    Client client1 = clientService.createClient("MultiClient1", "multi1@test.com");
    Client client2 = clientService.createClient("MultiClient2", "multi2@test.com");
    Client client3 = clientService.createClient("MultiClient3", "multi3@test.com");

    Long clientId1 = client1.getClientId();
    Long clientId2 = client2.getClientId();
    Long clientId3 = client3.getClientId();

    // Create multiple users for each client
    final TarsUser u1c1 = tarsUserService.createUser(clientId1, "u1c1", "u1c1@test.com", "user");
    final TarsUser u2c1 = tarsUserService.createUser(clientId1, "u2c1", "u2c1@test.com", "admin");
    final TarsUser u1c2 = tarsUserService.createUser(clientId2, "u1c2", "u1c2@test.com", "user");
    final TarsUser u2c2 = tarsUserService.createUser(clientId2, "u2c2", "u2c2@test.com", "user");
    final TarsUser u1c3 = tarsUserService.createUser(clientId3, "u1c3", "u1c3@test.com", "admin");

    // Verify user counts per client
    assertEquals(2, tarsUserService.listUsersByClientId(clientId1).size());
    assertEquals(2, tarsUserService.listUsersByClientId(clientId2).size());
    assertEquals(1, tarsUserService.listUsersByClientId(clientId3).size());

    // Verify all users have correct clientId
    assertEquals(clientId1, u1c1.getClientId());
    assertEquals(clientId1, u2c1.getClientId());
    assertEquals(clientId2, u1c2.getClientId());
    assertEquals(clientId2, u2c2.getClientId());
    assertEquals(clientId3, u1c3.getClientId());
  }

  /**
   * Integration test: Verify preferences can be set for multiple users.
   * Tests TarsService with multiple UserPreference entries.
   */
  @Test
  public void testMultipleUsersMultiplePreferences() {
    // Create client and multiple users
    Client client = clientService.createClient("MultiPrefClient", "multipref@test.com");
    Long clientId = client.getClientId();

    TarsUser user1 = tarsUserService.createUser(clientId, "pref_user1", "pref1@test.com", "user");
    TarsUser user2 = tarsUserService.createUser(clientId, "pref_user2", "pref2@test.com", "user");
    TarsUser user3 = tarsUserService.createUser(clientId, "pref_user3", "pref3@test.com", "user");

    Long userId1 = user1.getUserId();
    Long userId2 = user2.getUserId();
    Long userId3 = user3.getUserId();

    // Set preferences for each user
    UserPreference pref1 = new UserPreference(
        userId1, List.of("sunny"), List.of("25"), List.of("Miami"));
    UserPreference pref2 = new UserPreference(
        userId2, List.of("rainy"), List.of("15"), List.of("Seattle"));
    UserPreference pref3 = new UserPreference(
        userId3, List.of("snowy"), List.of("0"), List.of("Alaska"));

    assertTrue(tarsService.setUserPreference(pref1));
    assertTrue(tarsService.setUserPreference(pref2));
    assertTrue(tarsService.setUserPreference(pref3));

    // Verify all preferences are stored independently
    UserPreference retrieved1 = tarsService.getUserPreference(userId1);
    UserPreference retrieved2 = tarsService.getUserPreference(userId2);
    UserPreference retrieved3 = tarsService.getUserPreference(userId3);

    assertNotNull(retrieved1);
    assertNotNull(retrieved2);
    assertNotNull(retrieved3);

    assertTrue(retrieved1.getCityPreferences().contains("Miami"));
    assertTrue(retrieved2.getCityPreferences().contains("Seattle"));
    assertTrue(retrieved3.getCityPreferences().contains("Alaska"));
  }
}

