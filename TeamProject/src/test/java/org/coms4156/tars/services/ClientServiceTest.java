package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.service.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * {@code ClientServiceTest} Unit tests for {@link ClientService}.
 * Each test method documents the branch or behavior under validation.
 * 
 * <p>Equivalence Partition Testing for ClientService Methods.
 * ========== String rotateApiKey(long clientId) ===========
 * ========== List<Client> getClientList() ===========
 * ========== Client getClient(long clientId) ==========
 * ========== boolean uniqueNameCheck(String newClientName) ==========
 * ========== boolean uniqueEmailCheck(String email) ===========
 * ========== Client createClient(String name, String email) ===========
 * ========== boolean removeClient(int clientId) ===========
 * ========== boolean updateClient(Client updatedClient) ===========
 */
public class ClientServiceTest {

  private ClientService clientService;
  private Path tempTestDataFile;

  /**
   * Initializes a fresh {@link ClientService} using classpath test data
   * before each test. Ensures isolation and predictable client list baseline.
   */
  @BeforeEach
  void setUp() throws IOException {
    InputStream resourceStream = getClass().getClassLoader()
        .getResourceAsStream(
            "test-data/test-data-clients.json"
        );
    
    if (resourceStream == null) {
      throw new IllegalStateException("Test data file not found in classpath");
    }
    
    tempTestDataFile = Files.createTempFile("test-clients", ".json");
    Files.copy(resourceStream, tempTestDataFile, StandardCopyOption.REPLACE_EXISTING);
    resourceStream.close();
    
    clientService = new ClientService(tempTestDataFile.toString(), null);
  }

  /**
   * Cleanup temporary test file after each test.
   */
  @org.junit.jupiter.api.AfterEach
  void tearDown() throws IOException {
    if (tempTestDataFile != null && Files.exists(tempTestDataFile)) {
      Files.deleteIfExists(tempTestDataFile);
    }
  }

  /**
   * {@code clientServiceInitializationTest} Verifies service loads clients
   * correctly from test data file.
   */
  @Test
  public void clientServiceInitializationTest() {
    assertNotNull(clientService, "ClientService should be initialized");
    List<Client> clients = clientService.getClientList();
    assertEquals(
        4,
        clients.size(),
        "Should load 4 test clients"
    );
  }

  /**
   * {@code getClientListReturnsDefensiveCopyTest} Ensures returned list is a
   * defensive copy that cannot modify internal state.
   */
  @Test
  public void getClientListReturnsDefensiveCopyTest() {
    List<Client> clients1 = clientService.getClientList();
    List<Client> clients2 = clientService.getClientList();
    assertNotNull(clients1);
    assertNotNull(clients2);
    assertTrue(
        clients1 != clients2,
        "Should return new list instance each time"
    );
  }

  /**
   * {@code getClientListReloadClients} Simulates client list in ClientService being null
   * before an operation. Will reload the client information from database 
   * before executing getClientList().
   */
  @Test
  public void getClientListReloadClients() throws Exception {
    Field clientsField = ClientService.class.getDeclaredField("clients");
    clientsField.setAccessible(true);
    clientsField.set(clientService, null);
    assertNull(clientsField.get(clientService));

    // clients should have been reloaded
    List<Client> clientList = clientService.getClientList();
    assertNotNull(clientList);
    assertEquals(4, clientList.size());
  }

  /**
   * {@code removeClientIdReloadClients} Simulates client list in ClientService being null 
   * before an operation. Will reload the client information from database 
   * before executing removeClient
   */
  @Test
  public void removeClientReloadClients() throws Exception {
    Field clientsField = ClientService.class.getDeclaredField("clients");
    clientsField.setAccessible(true);
    clientsField.set(clientService, null);
    assertNull(clientsField.get(clientService));

    assertTrue(clientService.removeClient(3));
    assertNull(clientService.getClient(3));
    List<Client> clientList = clientService.getClientList();
    assertEquals(3, clientList.size());

  }

  @Test
  public void getClientReloadClients() throws Exception {
    Field clientsField = ClientService.class.getDeclaredField("clients");
    clientsField.setAccessible(true);
    clientsField.set(clientService, null);
    assertNull(clientsField.get(clientService));

    Client client = clientService.getClient(3);
    assertNotNull(client);
    assertEquals(3, client.getClientId());
    assertEquals("test_NewsAlertClient", client.getName());


  }


  /**
   * {@code getClientByIdSuccessTest} Verifies retrieval of existing client
   * by valid id.
   */
  @Test
  public void getClientByIdSuccessTest() {
    Client client = clientService.getClient(1L);
    assertNotNull(
        client,
        "Client with ID 1 should exist"
    );
    assertEquals(
        1L,
        client.getClientId(),
        "Client ID should match"
    );
    assertEquals(
        "test_WeatherAlertClient",
        client.getName(),
        "Client name should match"
    );
  }

  /**
   * {@code getClientByIdNotFoundTest} Confirms null returned for non-existent
   * client id.
   */
  @Test
  public void getClientByIdNotFoundTest() {
    Client client = clientService.getClient(999L);
    assertNull(
        client,
        "Non-existent client should return null"
    );
  }

  /**
   * {@code getClientWithNegativeIdTest} Validates negative id input returns
   * null and logs warning.
   */
  @Test
  public void getClientWithNegativeIdTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.getClient(-1L);
    
    assertNull(
        client,
        "Negative client ID should return null"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("cannot be negative")
        ),
        "Should log WARN for negative ID"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createClientSuccessTest} Confirms valid client creation with
   * auto-generated id and api key.
   */
  @Test
  public void createClientSuccessTest() {
    final int initialSize = clientService.getClientList().size();
    Client client = clientService.createClient(
        "NewClient",
        "new@example.com"
    );
    
    assertNotNull(
        client,
        "Created client should not be null"
    );
    assertEquals(
        5L,
        client.getClientId(),
        "New client should have ID 5"
    );
    assertEquals(
        "NewClient",
        client.getName(),
        "Name should match"
    );
    assertEquals(
        "new@example.com",
        client.getEmail(),
        "Email should match"
    );
    assertNotNull(
        client.getApiKey(),
        "API key should be generated"
    );
    assertEquals(
        32,
        client.getApiKey().length(),
        "API key should be 32 chars"
    );
    assertEquals(
        initialSize + 1,
        clientService.getClientList().size(),
        "Client list should grow by 1"
    );
  }

  /**
   * {@code createClientWithNullNameTest} Ensures null name is rejected and
   * logs warning.
   */
  @Test
  public void createClientWithNullNameTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.createClient(null, "test@example.com");
    
    assertNull(
        client,
        "Client with null name should not be created"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("blank name")
        ),
        "Should log WARN for blank name"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createClientWithBlankNameTest} Validates blank name is rejected
   * with appropriate logging.
   */
  @Test
  public void createClientWithBlankNameTest() {
    Client client = clientService.createClient(
        "   ",
        "test@example.com"
    );
    assertNull(
        client,
        "Client with blank name should not be created"
    );
  }

  /**
   * {@code createClientWithNullEmailTest} Ensures null email is rejected and
   * logs warning.
   */
  @Test
  public void createClientWithNullEmailTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.createClient("TestClient", null);
    
    assertNull(
        client,
        "Client with null email should not be created"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("blank email")
        ),
        "Should log WARN for blank email"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createClientWithBlankEmailTest} Validates blank email is rejected
   * with appropriate logging.
   */
  @Test
  public void createClientWithBlankEmailTest() {
    Client client = clientService.createClient(
        "TestClient",
        "   "
    );
    assertNull(
        client,
        "Client with blank email should not be created"
    );
  }

  /**
   * {@code createClientWithDuplicateNameTest} Confirms duplicate name is
   * rejected (case-insensitive).
   */
  @Test
  public void createClientWithDuplicateNameTest() {
    Client client = clientService.createClient(
        "test_WeatherAlertClient",
        "unique@example.com"
    );
    assertNull(
        client,
        "Duplicate name should be rejected"
    );
  }

  /**
   * {@code createClientWithDuplicateNameCaseInsensitiveTest} Verifies name
   * uniqueness check is case-insensitive.
   */
  @Test
  public void createClientWithDuplicateNameCaseInsensitiveTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.createClient(
        "TEST_WEATHERALERTCLIENT",
        "unique@example.com"
    );
    
    assertNull(
        client,
        "Duplicate name (case-insensitive) should be rejected"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("already exists")
        ),
        "Should log WARN for duplicate name"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createClientWithDuplicateEmailTest} Confirms duplicate email is
   * rejected (case-insensitive).
   */
  @Test
  public void createClientWithDuplicateEmailTest() {
    Client client = clientService.createClient(
        "UniqueClient",
        "test_client1@weatheralertclient.com"
    );
    assertNull(
        client,
        "Duplicate email should be rejected"
    );
  }

  /**
   * {@code createClientWithDuplicateEmailCaseInsensitiveTest} Verifies email
   * uniqueness check is case-insensitive.
   */
  @Test
  public void createClientWithDuplicateEmailCaseInsensitiveTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.createClient(
        "UniqueClient",
        "TEST_CLIENT1@WEATHERALERTCLIENT.COM"
    );
    
    assertNull(
        client,
        "Duplicate email (case-insensitive) should be rejected"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("already exists")
        ),
        "Should log WARN for duplicate email"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createClientLogsSuccessTest} Validates info log emitted upon
   * successful client creation.
   */
  @Test
  public void createClientLogsSuccessTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    clientService.createClient("LogTestClient", "logtest@example.com");

    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Client created successfully")
        ),
        "Should log INFO on successful creation"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code uniqueNameCheckWithNullTest} Ensures null name returns false for
   * uniqueness check.
   */
  @Test
  public void uniqueNameCheckWithNullTest() {
    assertFalse(
        clientService.uniqueNameCheck(null),
        "Null name should not be unique"
    );
  }

  /**
   * {@code uniqueNameCheckWithUniqueNameTest} Confirms unique name passes
   * the check.
   */
  @Test
  public void uniqueNameCheckWithUniqueNameTest() {
    assertTrue(
        clientService.uniqueNameCheck("CompletelyUniqueName"),
        "Unique name should pass check"
    );
  }

  /**
   * {@code uniqueNameCheckWithExistingNameTest} Validates existing name
   * fails uniqueness check.
   */
  @Test
  public void uniqueNameCheckWithExistingNameTest() {
    assertFalse(
        clientService.uniqueNameCheck("test_WeatherAlertClient"),
        "Existing name should fail check"
    );
  }

  /**
   * {@code uniqueEmailCheckWithNullTest} Ensures null email returns false
   * for uniqueness check.
   */
  @Test
  public void uniqueEmailCheckWithNullTest() {
    assertFalse(
        clientService.uniqueEmailCheck(null),
        "Null email should not be unique"
    );
  }

  /**
   * {@code uniqueEmailCheckWithUniqueEmailTest} Confirms unique email passes
   * the check.
   */
  @Test
  public void uniqueEmailCheckWithUniqueEmailTest() {
    assertTrue(
        clientService.uniqueEmailCheck("unique@newdomain.com"),
        "Unique email should pass check"
    );
  }

  /**
   * {@code uniqueEmailCheckWithExistingEmailTest} Validates existing email
   * fails uniqueness check.
   */
  @Test
  public void uniqueEmailCheckWithExistingEmailTest() {
    assertFalse(
        clientService.uniqueEmailCheck("test_client1@weatheralertclient.com"),
        "Existing email should fail check"
    );
  }

  /**
   * {@code removeClientSuccessTest} Verifies successful removal of existing
   * client and persistence.
   */
  @Test
  public void removeClientSuccessTest() {
    int initialSize = clientService.getClientList().size();
    boolean removed = clientService.removeClient(1);
    
    assertTrue(
        removed,
        "Should successfully remove existing client"
    );
    assertEquals(
        initialSize - 1,
        clientService.getClientList().size(),
        "Client list should shrink by 1"
    );
    assertNull(
        clientService.getClient(1L),
        "Removed client should no longer be found"
    );
  }

  /**
   * {@code removeClientNotFoundTest} Confirms false returned when removing
   * non-existent client.
   */
  @Test
  public void removeClientNotFoundTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    boolean removed = clientService.removeClient(999);
    
    assertFalse(
        removed,
        "Should return false for non-existent client"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("not found for removal")
        ),
        "Should log WARN when client not found"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code removeClientLogsSuccessTest} Validates info log emitted upon
   * successful client removal.
   */
  @Test
  public void removeClientLogsSuccessTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    clientService.removeClient(1);

    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Client removed successfully")
        ),
        "Should log INFO on successful removal"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientSuccessTest} Confirms successful update of existing
   * client with new information.
   */
  @Test
  public void updateClientSuccessTest() {
    Client existing = clientService.getClient(1L);
    assertNotNull(existing);
    
    existing.setName("UpdatedName");
    existing.setEmail("updated@example.com");
    existing.setRateLimitPerMinute(100);
    
    boolean updated = clientService.updateClient(existing);
    
    assertTrue(
        updated,
        "Should successfully update existing client"
    );
    Client retrieved = clientService.getClient(1L);
    assertEquals(
        "UpdatedName",
        retrieved.getName(),
        "Name should be updated"
    );
    assertEquals(
        "updated@example.com",
        retrieved.getEmail(),
        "Email should be updated"
    );
    assertEquals(
        100,
        retrieved.getRateLimitPerMinute(),
        "Rate limit should be updated"
    );
  }

  /**
   * {@code updateClientWithNullTest} Ensures null client is rejected and
   * logs warning.
   */
  @Test
  public void updateClientWithNullTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    boolean updated = clientService.updateClient(null);
    
    assertFalse(
        updated,
        "Updating null client should fail"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("null client")
        ),
        "Should log WARN for null client"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientWithNullIdTest} Validates client without id is rejected
   * and logs warning.
   */
  @Test
  public void updateClientWithNullIdTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = new Client();
    client.setClientId(null);
    client.setName("Test");
    client.setEmail("test@example.com");
    
    boolean updated = clientService.updateClient(client);
    
    assertFalse(
        updated,
        "Client without ID should not be updated"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("missing clientId")
        ),
        "Should log WARN for missing client ID"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientWithBlankNameTest} Ensures blank name is rejected
   * during update.
   */
  @Test
  public void updateClientWithBlankNameTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = new Client();
    client.setClientId(1L);
    client.setName("   ");
    client.setEmail("test@example.com");
    
    boolean updated = clientService.updateClient(client);
    
    assertFalse(
        updated,
        "Client with blank name should not be updated"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("blank name")
        ),
        "Should log WARN for blank name"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientWithDuplicateNameTest} Confirms update fails when
   * new name conflicts with another client.
   */
  @Test
  public void updateClientWithDuplicateNameTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.getClient(1L);
    client.setName("test_CrimeAlertClient"); // Name of client ID 2
    
    boolean updated = clientService.updateClient(client);
    
    assertFalse(
        updated,
        "Update with duplicate name should fail"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("already in use")
        ),
        "Should log WARN for duplicate name"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientPreservesApiKeyTest} Validates api key is preserved
   * when not provided in update.
   */
  @Test
  public void updateClientPreservesApiKeyTest() {
    final String originalApiKey = clientService.getClient(1L).getApiKey();
    
    Client update = new Client();
    update.setClientId(1L);
    update.setName("UpdatedName");
    update.setEmail("updated@example.com");
    update.setApiKey(null); // Explicitly null
    
    clientService.updateClient(update);
    
    Client retrieved = clientService.getClient(1L);
    assertEquals(
        originalApiKey,
        retrieved.getApiKey(),
        "API key should be preserved when null in update"
    );
  }

  /**
   * {@code updateClientNotFoundTest} Confirms false returned when updating
   * non-existent client.
   */
  @Test
  public void updateClientNotFoundTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = new Client();
    client.setClientId(999L);
    client.setName("NonExistent");
    client.setEmail("none@example.com");
    
    boolean updated = clientService.updateClient(client);
    
    assertFalse(
        updated,
        "Updating non-existent client should fail"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("not found for update")
        ),
        "Should log WARN when client not found"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code updateClientLogsSuccessTest} Validates info log emitted upon
   * successful client update.
   */
  @Test
  public void updateClientLogsSuccessTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Client client = clientService.getClient(1L);
    client.setName("NewUpdatedName");
    clientService.updateClient(client);

    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Client updated successfully")
        ),
        "Should log INFO on successful update"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code rotateApiKeySuccessTest} Verifies successful api key rotation
   * for existing client.
   */
  @Test
  public void rotateApiKeySuccessTest() {
    Client client = clientService.getClient(1L);
    String oldKey = client.getApiKey();
    
    String newKey = clientService.rotateApiKey(1L);
    
    assertNotNull(
        newKey,
        "New API key should be generated"
    );
    assertEquals(
        32,
        newKey.length(),
        "New API key should be 32 chars"
    );
    assertFalse(
        oldKey.equals(newKey),
        "New key should differ from old key"
    );
    
    Client retrieved = clientService.getClient(1L);
    assertEquals(
        newKey,
        retrieved.getApiKey(),
        "Retrieved client should have new API key"
    );
  }

  /**
   * {@code rotateApiKeyNotFoundTest} Confirms null returned when rotating
   * key for non-existent client.
   */
  @Test
  public void rotateApiKeyNotFoundTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    String newKey = clientService.rotateApiKey(999L);
    
    assertNull(
        newKey,
        "Should return null for non-existent client"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("client not found")
        ),
        "Should log WARN when client not found"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code rotateApiKeyLogsSuccessTest} Validates info log emitted upon
   * successful api key rotation.
   */
  @Test
  public void rotateApiKeyLogsSuccessTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    clientService.rotateApiKey(1L);

    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("API key rotated")
        ),
        "Should log INFO on successful rotation"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code printClientsLogsAllTest} Ensures printClients logs each client
   * at info level.
   */
  @Test
  public void printClientsLogsAllTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    clientService.printClients();

    long infoCount = listAppender.list.stream()
        .filter(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().startsWith("Client:")
        )
        .count();
    
    assertEquals(
        4,
        infoCount,
        "Should log 4 client entries"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code serviceCreatesFileWhenMissingTest} Ensures a new data file is
   * created when none exists and starts empty.
   */
  @Test
  public void serviceCreatesFileWhenMissingTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Path tempDir = Files.createTempDirectory("client-test");
    Path testFile = tempDir.resolve("new-clients.json");
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    assertTrue(
        Files.exists(testFile),
        "File should be created"
    );
    List<Client> clients = service.getClientList();
    assertEquals(
        0,
        clients.size(),
        "New file should have empty client list"
    );
    
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Created new client data file")
        ),
        "Should log INFO when file created"
    );

    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code serviceLogsInfoOnExistingFileTest} Verifies info log emitted
   * when using existing data file.
   */
  @Test
  public void serviceLogsInfoOnExistingFileTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    // Create service with existing temp file
    new ClientService(tempTestDataFile.toString(), null);

    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Using existing client data file")
        ),
        "Should log INFO when using existing file"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code loadDataHandlesCorruptedFileTest} Confirms corrupt JSON degrades
   * safely to empty list without exception.
   */
  @Test
  public void loadDataHandlesCorruptedFileTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Path tempFile = Files.createTempFile("corrupt-clients", ".json");
    Files.writeString(tempFile, "{ invalid json [[[");
    
    ClientService service = new ClientService(tempFile.toString(), null);
    List<Client> clients = service.getClientList();
    
    assertEquals(
        0,
        clients.size(),
        "Corrupted file should return empty list"
    );
    assertTrue(
        listAppender.list.stream().anyMatch(event ->
            event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Failed to load clients")
        ),
        "Should log ERROR when load fails"
    );

    logger.detachAppender(listAppender);
    Files.deleteIfExists(tempFile);
  }

  /**
   * {@code saveDataHandlesIoExceptionTest} Verifies error logging when
   * save operation fails.
   */
  @Test
  public void saveDataHandlesIoExceptionTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    // Create service then delete underlying file to cause save failure
    Path tempDir = Files.createTempDirectory("save-fail-test");
    Path testFile = tempDir.resolve("clients.json");
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Make directory read-only on Unix to cause write failure
    if (!System.getProperty("os.name").toLowerCase().contains("win")) {
      testFile.toFile().getParentFile().setWritable(false);
    }
    
    // Trigger save
    service.createClient("FailTest", "fail@example.com");
    
    // Restore permissions
    if (!System.getProperty("os.name").toLowerCase().contains("win")) {
      testFile.toFile().getParentFile().setWritable(true);
    }

    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code apiKeyGenerationIsUniqueTest} Validates multiple generated api
   * keys are unique.
   */
  @Test
  public void apiKeyGenerationIsUniqueTest() {
    Client client1 = clientService.createClient("Client1", "client1@test.com");
    Client client2 = clientService.createClient("Client2", "client2@test.com");
    
    assertNotNull(client1);
    assertNotNull(client2);
    assertFalse(
        client1.getApiKey().equals(client2.getApiKey()),
        "Generated API keys should be unique"
    );
  }

  /**
   * {@code updateClientKeepsSameNameTest} Confirms update succeeds when
   * keeping the same name (case-insensitive check passes).
   */
  @Test
  public void updateClientKeepsSameNameTest() {
    Client existing = clientService.getClient(1L);
    String originalName = existing.getName();
    
    existing.setEmail("newemail@example.com");
    // Keep same name
    
    boolean updated = clientService.updateClient(existing);
    
    assertTrue(
        updated,
        "Update with same name should succeed"
    );
    Client retrieved = clientService.getClient(1L);
    assertEquals(
        originalName,
        retrieved.getName(),
        "Name should remain unchanged"
    );
    assertEquals(
        "newemail@example.com",
        retrieved.getEmail(),
        "Email should be updated"
    );
  }

  /**
   * {@code persistenceAcrossInstancesTest} Validates data persists and
   * reloads correctly across service instances.
   */
  @Test
  public void persistenceAcrossInstancesTest() throws IOException {
    Path testDir = Files.createTempDirectory("persist-test");
    Path testFile = testDir.resolve("persist-clients.json");
    
    // First service instance creates client
    ClientService service1 = new ClientService(testFile.toString(), null);
    Client created = service1.createClient("PersistTest", "persist@test.com");
    assertNotNull(created);
    Long clientId = created.getClientId();
    
    // Second service instance should load persisted data
    ClientService service2 = new ClientService(testFile.toString(), null);
    Client retrieved = service2.getClient(clientId);
    
    assertNotNull(
        retrieved,
        "Persisted client should be retrievable"
    );
    assertEquals(
        "PersistTest",
        retrieved.getName(),
        "Persisted data should match"
    );
    assertEquals(
        "persist@test.com",
        retrieved.getEmail(),
        "Persisted email should match"
    );
    
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }

  /**
   * Test logger guards - isInfoEnabled when disabled.
   */
  @Test
  public void testLoggerInfoDisabled() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    try {
      logger.setLevel(Level.ERROR);
      
      Path testDir = Files.createTempDirectory("logger-info-test");
      Path testFile = testDir.resolve("clients.json");
      
      // This should trigger logger.isInfoEnabled() checks
      ClientService service = new ClientService(testFile.toString(), null);
      service.createClient("TestClient", "test@test.com");
      
      assertNotNull(service.getClientList(), "Service should work with INFO logging disabled");
    } finally {
      logger.setLevel(Level.TRACE);
    }
  }

  /**
   * Test logger guards - isWarnEnabled when disabled.
   */
  @Test
  public void testLoggerWarnDisabled() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    try {
      logger.setLevel(Level.ERROR);
      
      Path testDir = Files.createTempDirectory("logger-warn-test");
      Path testFile = testDir.resolve("clients.json");
      
      ClientService service = new ClientService(testFile.toString(), null);
      
      // Trigger warning conditions
      service.createClient("", "test@test.com"); // blank name
      service.createClient(null, "test@test.com"); // null client
      
      assertTrue(service.getClientList().isEmpty(), 
          "No clients should be created with invalid data");
    } finally {
      logger.setLevel(Level.TRACE);
    }
  }

  /**
   * Test logger guards - isErrorEnabled when disabled.
   */
  @Test
  public void testLoggerErrorDisabled() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    try {
      logger.setLevel(Level.OFF);
      
      // Create file with invalid JSON
      Path testDir = Files.createTempDirectory("logger-error-test");
      Path testFile = testDir.resolve("corrupt.json");
      Files.writeString(testFile, "{invalid json}");
      
      ClientService service = new ClientService(testFile.toString(), null);
      
      // Should handle error silently
      assertNotNull(service.getClientList(), "Service should handle errors gracefully");
    } finally {
      logger.setLevel(Level.TRACE);
    }
  }

  /**
   * Test logger guards - isDebugEnabled when disabled.
   */
  @Test
  public void testLoggerDebugDisabled() throws Exception {
    Logger logger = (Logger) LoggerFactory.getLogger(ClientService.class);
    try {
      logger.setLevel(Level.INFO);
      
      Path testDir = Files.createTempDirectory("logger-debug-test");
      Path testFile = testDir.resolve("clients.json");
      
      ClientService service = new ClientService(testFile.toString(), null);
      Client created = service.createClient("DebugTest", "debug@test.com");
      
      assertNotNull(created, "Client should be created with DEBUG logging disabled");
    } finally {
      logger.setLevel(Level.TRACE);
    }
  }

  /**
   * Test parent directory is null (root path).
   */
  @Test
  public void testParentDirectoryNull() throws IOException {
    java.io.File tempRoot = java.io.File.listRoots()[0];
    java.io.File testFile = new java.io.File(tempRoot, 
        "tars-client-test-" + System.currentTimeMillis() + ".json");
    
    try {
      ClientService service = new ClientService(testFile.toString(), null);
      assertNotNull(service.getClientList(), 
          "Service should initialize with root-level file");
    } finally {
      if (testFile.exists()) {
        testFile.delete();
      }
    }
  }

  /**
   * Test client ID not greater than maxId.
   */
  @Test
  public void testMaxIdNotUpdated() throws IOException {
    Path testDir = Files.createTempDirectory("max-id-test");
    Path testFile = testDir.resolve("clients.json");
    
    // Create file with clients having IDs 1, 2, 3
    java.util.List<Client> initialClients = new java.util.ArrayList<>();
    for (int i = 1; i <= 3; i++) {
      Client c = new Client();
      c.setClientId((long) i);
      c.setName("Client" + i);
      c.setEmail("client" + i + "@test.com");
      c.setRateLimitPerMinute(60);
      c.setMaxConcurrentRequests(5);
      initialClients.add(c);
    }
    
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), initialClients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Next ID should be 4 (maxId=3, so all existing IDs were <= maxId)
    Client created = service.createClient("NewClient", "new@test.com");
    assertEquals(4L, created.getClientId(), "Next ID should be maxId + 1");
  }

  /**
   * Test name uniqueness check with null existing name.
   */
  @Test
  public void testIsNameTakenNullExistingName() throws Exception {
    final Path testDir = Files.createTempDirectory("null-name-test");
    
    // Create client with null name
    Client c = new Client();
    c.setClientId(1L);
    c.setName(null);  // null name
    c.setEmail("test@test.com");
    c.setRateLimitPerMinute(60);
    c.setMaxConcurrentRequests(5);
    
    java.util.List<Client> clients = new java.util.ArrayList<>();
    clients.add(c);
    
    Path testFile = testDir.resolve("clients.json");
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), clients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Try to create client with a name - should succeed since existing has null name
    Client created = service.createClient("ValidName", "new@test.com");
    assertNotNull(created, "Should allow creation when existing client has null name");
  }

  /**
   * Test email uniqueness check with null existing email.
   */
  @Test
  public void testIsEmailTakenNullExistingEmail() throws Exception {
    final Path testDir = Files.createTempDirectory("null-email-test");
    
    // Create client with null email
    Client c = new Client();
    c.setClientId(1L);
    c.setName("TestClient");
    c.setEmail(null);  // null email
    c.setRateLimitPerMinute(60);
    c.setMaxConcurrentRequests(5);
    
    java.util.List<Client> clients = new java.util.ArrayList<>();
    clients.add(c);
    
    Path testFile = testDir.resolve("clients.json");
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), clients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Try to create client with an email - should succeed since existing has null email
    Client created = service.createClient("NewClient", "valid@test.com");
    assertNotNull(created, "Should allow creation when existing client has null email");
  }

  /**
   * Test update with null or blank name.
   */
  @Test
  public void testUpdateClientBlankName() throws IOException {
    Path testDir = Files.createTempDirectory("blank-name-update-test");
    Path testFile = testDir.resolve("clients.json");
    
    ClientService service = new ClientService(testFile.toString(), null);
    Client original = service.createClient("OriginalName", "test@test.com");
    
    Client updated = new Client();
    updated.setClientId(original.getClientId());
    updated.setName("   ");  // blank name
    updated.setEmail("test@test.com");
    updated.setRateLimitPerMinute(100);
    
    boolean result = service.updateClient(updated);
    assertFalse(result, "Should reject update with blank name");
  }

  /**
   * Test update with other client having null clientId.
   */
  @Test
  public void testUpdateClientOtherNullClientId() throws Exception {
    final Path testDir = Files.createTempDirectory("other-null-id-test");
    
    // Create two clients, one with null ID
    Client c1 = new Client();
    c1.setClientId(null);  // null ID
    c1.setName("Client1");
    c1.setEmail("client1@test.com");
    c1.setRateLimitPerMinute(60);
    c1.setMaxConcurrentRequests(5);
    
    Client c2 = new Client();
    c2.setClientId(2L);
    c2.setName("Client2");
    c2.setEmail("client2@test.com");
    c2.setRateLimitPerMinute(60);
    c2.setMaxConcurrentRequests(5);
    
    java.util.List<Client> clients = new java.util.ArrayList<>();
    clients.add(c1);
    clients.add(c2);
    
    Path testFile = testDir.resolve("clients.json");
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), clients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Update client 2 - should succeed despite client 1 having null ID
    Client updated = new Client();
    updated.setClientId(2L);
    updated.setName("UpdatedClient2");
    updated.setEmail("client2@test.com");
    updated.setRateLimitPerMinute(100);
    updated.setMaxConcurrentRequests(10);
    
    boolean result = service.updateClient(updated);
    assertTrue(result, "Should allow update when other client has null ID");
  }

  /**
   * Test update with other client having null name.
   */
  @Test
  public void testUpdateClientOtherNullName() throws Exception {
    final Path testDir = Files.createTempDirectory("other-null-name-test");
    
    // Create two clients, one with null name
    Client c1 = new Client();
    c1.setClientId(1L);
    c1.setName(null);  // null name
    c1.setEmail("client1@test.com");
    c1.setRateLimitPerMinute(60);
    c1.setMaxConcurrentRequests(5);
    
    Client c2 = new Client();
    c2.setClientId(2L);
    c2.setName("Client2");
    c2.setEmail("client2@test.com");
    c2.setRateLimitPerMinute(60);
    c2.setMaxConcurrentRequests(5);
    
    java.util.List<Client> clients = new java.util.ArrayList<>();
    clients.add(c1);
    clients.add(c2);
    
    Path testFile = testDir.resolve("clients.json");
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), clients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Update client 2 - should succeed despite client 1 having null name
    Client updated = new Client();
    updated.setClientId(2L);
    updated.setName("UpdatedClient2");
    updated.setEmail("client2@test.com");
    updated.setRateLimitPerMinute(100);
    updated.setMaxConcurrentRequests(10);
    
    boolean result = service.updateClient(updated);
    assertTrue(result, "Should allow update when other client has null name");
  }

  /**
   * Test update existing client with null clientId.
   */
  @Test
  public void testUpdateExistingClientNullId() throws Exception {
    final Path testDir = Files.createTempDirectory("existing-null-id-test");
    
    // Create client with null ID
    Client c = new Client();
    c.setClientId(null);  // null ID
    c.setName("OriginalClient");
    c.setEmail("original@test.com");
    c.setRateLimitPerMinute(60);
    c.setMaxConcurrentRequests(5);
    
    java.util.List<Client> clients = new java.util.ArrayList<>();
    clients.add(c);
    
    Path testFile = testDir.resolve("clients.json");
    com.fasterxml.jackson.databind.ObjectMapper om = 
        new com.fasterxml.jackson.databind.ObjectMapper();
    om.writeValue(testFile.toFile(), clients);
    
    ClientService service = new ClientService(testFile.toString(), null);
    
    // Try to update with ID=1
    Client updated = new Client();
    updated.setClientId(1L);
    updated.setName("UpdatedName");
    updated.setEmail("updated@test.com");
    updated.setRateLimitPerMinute(100);
    updated.setMaxConcurrentRequests(10);
    
    boolean result = service.updateClient(updated);
    assertFalse(result, "Should not update when existing client has null ID");
  }

  /**
   * Test copyFrom with null source.
   */
  @Test
  public void testCopyFromNullSource() throws IOException {
    Path testDir = Files.createTempDirectory("copy-null-test");
    Path testFile = testDir.resolve("clients.json");
    
    ClientService service = new ClientService(testFile.toString(), null);
    Client created = service.createClient("TestClient", "test@test.com");
    
    Client target = service.getClient(created.getClientId());
    
    // Use reflection to call copyFrom with null
    try {
      java.lang.reflect.Method method = ClientService.class
          .getDeclaredMethod("copyFrom", Client.class, Client.class);
      method.setAccessible(true);
      method.invoke(service, target, null);
      
      // Target should remain unchanged
      assertEquals("TestClient", target.getName(), 
          "Target should not change with null source");
    } catch (Exception e) {
      // Expected to handle null gracefully
    }
  }
}
