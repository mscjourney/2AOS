package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * {@code TarsUserServiceTest} Unit tests for {@link TarsUserService}.
 * Each test method documents the branch or behavior under validation.
 */
public class TarsUserServiceTest {

  private TarsUserService userService;
  private Path tempTestDataFile;

  /**
   * Initializes a fresh {@link TarsUserService} using classpath test data
   * before each test. Ensures isolation and predictable user list baseline.
   */
  @BeforeEach
  void setUp() throws IOException {
    // Copy classpath resource to temp file since service expects file path
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing: test-data-users.json");
      tempTestDataFile = Files.createTempFile("test-users", ".json");
      Files.copy(resourceStream, tempTestDataFile, StandardCopyOption.REPLACE_EXISTING);
    }
    
    userService = new TarsUserService(tempTestDataFile.toString(), null);
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
   * {@code userServiceInitializationTest} Verifies initial load yields first user
   * with expected id and username.
   */
  @Test
  public void userServiceInitializationTest() {
    assertNotNull(userService, "UserService should be initialized");
    List<TarsUser> tarsUsers = userService.listUsers();
    TarsUser user1 = tarsUsers.get(0);
    assertEquals(
        1L,
        user1.getUserId(),
        "First user's ID should be 1"
    );
    assertEquals(
        "test_alice",
        user1.getUsername(),
        "First user's username should be 'test_alice'"
    );
  }

  /**
   * {@code getUserByIdTest} Exercises lookup by null, missing, and existing id to
   * confirm correct returns.
   */
  @Test
  public void getUserByIdTest() {
    TarsUser user2 = userService.findById(null);
    assertNull(user2, "Fetching user with null ID should return null");
    TarsUser user3 = userService.findById(999L);
    assertNull(user3, "Fetching non-existent user should return null");
    TarsUser user1 = userService.findById(1L);
    assertNotNull(
        user1,
        "Fetching existing user should not return null"
    );
    assertEquals(
        "test_alice",
        user1.getUsername(),
        "Fetched user's username should be 'test_alice'"
    );
  }

  /**
   * {@code createNullUserTest} Ensures createUser rejects completely null input
   * returning null safely.
   */
  @Test
  public void createNullUserTest() {
    TarsUser result = userService.createUser(null, null, null, null);
    assertNull(result, "Creating a null user should return null");
  }

  /**
   * {@code createUserWithLimitedDataTest} Validates rejection when required
   * fields (username, role) are absent.
   */
  @Test
  public void createUserWithLimitedDataTest() {
    TarsUser user = userService.createUser(5L, null, "test_user@client5.com", null);
    assertNull(user, "Creating user with limited data should return null");
  }

  /**
   * {@code createValidUserTest} Confirms a properly formed user is persisted,
   * assigned next id, and retrievable.
   */
  @Test
  public void createValidUserTest() {
    final int initialSize = userService.listUsers().size();
    TarsUser user = userService.createUser(
        5L,
        "test_eve",
        "test_eve@client5.com",
        "user"
    );
    assertNotNull(
        user,
        "Creating a valid user should not return null"
    );
    user.toString(); // Exercise toString (no assertion needed)
    // ID should be max existing ID (4) + 1 = 5
    assertEquals(
        5L,
        user.getUserId(),
        "Created user's ID should be 5"
    );
    assertEquals(
        "test_eve",
        user.getUsername(),
        "Created user's username should be 'test_eve'"
    );
    assertEquals(
        user.toString(),
        userService.findById(5L).toString(),
        "Fetched user should match the created user"
    );
    assertEquals(
        initialSize + 1,
        userService.listUsers().size(),
        "User list size should increase by 1"
    );
  }

  /**
   * {@code deactivateUserSuccessTest} Verifies an active user becomes inactive
   * and operation signals success.
   */
  @Test
  public void deactivateUserSuccessTest() {
    boolean result = userService.deactivateUser(1L);
    assertTrue(result, "Deactivating existing user should succeed");
    TarsUser deactivatedUser = userService.findById(1L);
    assertNotNull(deactivatedUser);
    assertFalse(
        deactivatedUser.getActive(),
        "User should be inactive"
    );
  }

  /**
   * {@code deactivateNonExistentUserTest} Checks deactivation gracefully fails
   * for an unknown user id.
   */
  @Test
  public void deactivateNonExistentUserTest() {
    boolean result = userService.deactivateUser(999L);
    assertFalse(result, "Deactivating non-existent user should fail");
  }

  /**
   * {@code deactivateUserWithNullIdTest} Ensures null id input produces a false
   * result (no exception thrown).
   */
  @Test
  public void deactivateUserWithNullIdTest() {
    boolean result = userService.deactivateUser(null);
    assertFalse(result, "Deactivating user with null ID should fail");
  }

  /**
   * {@code updateLastLoginSuccessTest} Confirms timestamp refresh occurs for an
   * existing user and is non-empty.
   */
  @Test
  public void updateLastLoginSuccessTest() throws InterruptedException {
    TarsUser userBefore = userService.findById(1L);
    assertNotNull(userBefore, "User should exist before update");
    String loginBefore = userBefore.getLastLogin();
    
    // Add small delay to ensure timestamp difference
    Thread.sleep(2);
    
    boolean result = userService.updateLastLogin(1L);
    assertTrue(result, "Updating last login for existing user should succeed");
    TarsUser userAfter = userService.findById(1L);
    String loginAfter = userAfter.getLastLogin();
    assertNotEquals(
        loginBefore,
        loginAfter,
        "Last login should be updated"
    );
    assertFalse(
        loginAfter.isEmpty(),
        "Last login should not be empty"
    );
  }

  /**
   * {@code updateLastLoginNonExistentUserTest} Validates update fails cleanly
   * when user id does not exist.
   */
  @Test
  public void updateLastLoginNonExistentUserTest() {
    boolean result = userService.updateLastLogin(999L);
    assertFalse(result, "Updating last login for non-existent user should fail");
  }

  /**
   * {@code updateLastLoginNullIdTest} Ensures null id yields false without
   * throwing or mutating state.
   */
  @Test
  public void updateLastLoginNullIdTest() {
    boolean result = userService.updateLastLogin(null);
    assertFalse(result, "Updating last login with null ID should fail");
  }

  /**
   * {@code existsByClientIdAndUserEmailFoundTest} Confirms email presence is
   * detected for matching client.
   */
  @Test
  public void existsByClientIdAndUserEmailFoundTest() {
    boolean exists = userService.existsByClientIdAndUserEmail(1L, "test_alice@gmail.com");
    assertTrue(exists, "Existing email for client should be found");
  }

  /**
   * {@code existsByClientIdAndUserEmailCaseInsensitiveTest} Verifies email match
   * ignoring case differences.
   */
  @Test
  public void existsByClientIdAndUserEmailCaseInsensitiveTest() {
    boolean exists = userService.existsByClientIdAndUserEmail(1L, "TEST_ALICE@GMAIL.COM");
    assertTrue(exists, "Email check should be case-insensitive");
  }

  /**
   * {@code existsByClientIdAndUserEmailNotFoundTest} Asserts a non-existent
   * email returns false for client.
   */
  @Test
  public void existsByClientIdAndUserEmailNotFoundTest() {
    boolean exists = userService.existsByClientIdAndUserEmail(1L, "nonexistent@test.com");
    assertFalse(exists, "Non-existent email should not be found");
  }

  /**
   * {@code existsByClientIdAndUserEmailDifferentClientTest} Ensures an email on
   * another client is not reported.
   */
  @Test
  public void existsByClientIdAndUserEmailDifferentClientTest() {
    boolean exists = userService.existsByClientIdAndUserEmail(999L, "test_alice@gmail.com");
    assertFalse(exists, "Email from different client should not be found");
  }

  /**
   * {@code existsByClientIdAndUserEmailNullParamsTest} Validates null client or
   * email short-circuits to false.
   */
  @Test
  public void existsByClientIdAndUserEmailNullParamsTest() {
    assertFalse(userService.existsByClientIdAndUserEmail(null, "test@test.com"));
    assertFalse(userService.existsByClientIdAndUserEmail(1L, null));
    assertFalse(userService.existsByClientIdAndUserEmail(null, null));
  }

  /**
   * {@code createUserWithBlankUsernameTest} Rejects creation when trimmed
   * username is empty whitespace.
   */
  @Test
  public void createUserWithBlankUsernameTest() {
    TarsUser user = userService.createUser(5L, "  ", "test@test.com", "user");
    assertNull(user, "Creating user with blank username should return null");
  }

  /**
   * {@code createUserWithBlankEmailTest} Rejects creation when trimmed email
   * value is empty whitespace.
   */
  @Test
  public void createUserWithBlankEmailTest() {
    TarsUser user = userService.createUser(5L, "testuser", "  ", "user");
    assertNull(user, "Creating user with blank email should return null");
  }

  /**
   * {@code createUserWithBlankRoleTest} Ensures blank role causes createUser to
   * return null (validation path).
   */
  @Test
  public void createUserWithBlankRoleTest() {
    TarsUser user = userService.createUser(5L, "testuser", "test@test.com", "  ");
    assertNull(user, "Creating user with blank role should return null");
  }

  /**
   * {@code createUserWithWhitespaceTrimmingTest} Confirms leading/trailing
   * whitespace is trimmed for all fields.
   */
  @Test
  public void createUserWithWhitespaceTrimmingTest() {
    TarsUser user = userService.createUser(
        5L,
        "  testuser  ",
        "  test@test.com  ",
        "  admin  "
    );
    assertNotNull(
        user,
        "Creating user with whitespace should succeed"
    );
    assertEquals(
        "testuser",
        user.getUsername(),
        "Username should be trimmed"
    );
    assertEquals(
        "test@test.com",
        user.getEmail(),
        "Email should be trimmed"
    );
    assertEquals(
        "admin",
        user.getRole(),
        "Role should be trimmed"
    );
  }

  /**
   * {@code existsByClientIdAndUsernameFoundTest} Asserts existing username is
   * detected for given client id.
   */
  @Test
  public void existsByClientIdAndUsernameFoundTest() {
    boolean exists = userService.existsByClientIdAndUsername(1L, "test_alice");
    assertTrue(exists, "Existing username for client should be found");
  }

  /**
   * {@code existsByClientIdAndUsernameCaseInsensitiveTest} Verifies username
   * matching ignores letter case.
   */
  @Test
  public void existsByClientIdAndUsernameCaseInsensitiveTest() {
    boolean exists = userService.existsByClientIdAndUsername(1L, "TEST_ALICE");
    assertTrue(exists, "Username check should be case-insensitive");
  }

  /**
   * {@code existsByClientIdAndUsernameNotFoundTest} Ensures false returned when
   * username absent for client.
   */
  @Test
  public void existsByClientIdAndUsernameNotFoundTest() {
    boolean exists = userService.existsByClientIdAndUsername(1L, "nonexistent");
    assertFalse(exists, "Non-existent username should not be found");
  }

  /**
   * {@code existsByClientIdAndUsernameDifferentClientTest} Confirms identical
   * username on other client not counted.
   */
  @Test
  public void existsByClientIdAndUsernameDifferentClientTest() {
    boolean exists = userService.existsByClientIdAndUsername(999L, "test_alice");
    assertFalse(exists, "Username from different client should not be found");
  }

  /**
   * {@code existsByClientIdAndUsernameNullParamsTest} Validates null client or
   * username yields false immediately.
   */
  @Test
  public void existsByClientIdAndUsernameNullParamsTest() {
    assertFalse(userService.existsByClientIdAndUsername(null, "test"));
    assertFalse(userService.existsByClientIdAndUsername(1L, null));
    assertFalse(userService.existsByClientIdAndUsername(null, null));
  }

  /**
   * {@code listUsersReturnsCorrectCountTest} Ensures fixture loads expected
   * number of initial users.
   */
  @Test
  public void listUsersReturnsCorrectCountTest() {
    List<TarsUser> users = userService.listUsers();
    assertEquals(4, users.size(), "Should return 4 test users");
  }

  /**
   * {@code listUsersReturnsUnmodifiableListTest} Confirms returned list cannot
   * be structurally modified.
   */
  @Test
  public void listUsersReturnsUnmodifiableListTest() {
    List<TarsUser> users = userService.listUsers();
    assertThrows(
        UnsupportedOperationException.class,
        () -> users.add(new TarsUser(99L, "hacker", "hacker@test.com", "admin")),
        "Returned list should be unmodifiable"
    );
  }

  /**
   * {@code userIdAutoIncrementTest} Verifies sequential creations get strictly
   * increasing assigned ids.
   */
  @Test
  public void userIdAutoIncrementTest() {
    TarsUser user1 = userService.createUser(5L, "user1", "user1@test.com", "user");
    TarsUser user2 = userService.createUser(5L, "user2", "user2@test.com", "user");
    assertNotNull(user1);
    assertNotNull(user2);
    assertTrue(
        user2.getUserId() > user1.getUserId(),
        "User IDs should auto-increment"
    );
  }

  /**
   * {@code userAttributesSetCorrectlyTest} Asserts defaults (active, timestamps,
   * lastLogin) are initialized correctly.
   */
  @Test
  public void userAttributesSetCorrectlyTest() {
    TarsUser user = userService.createUser(
        5L,
        "newuser",
        "newuser@test.com",
        "admin"
    );
    assertNotNull(
        user.getUserId(),
        "User ID should be set"
    );
    assertTrue(
        user.getActive(),
        "New user should be active"
    );
    assertNotNull(
        user.getSignUpDate(),
        "Sign up date should be set"
    );
    assertFalse(
        user.getSignUpDate().isEmpty(),
        "Sign up date should not be empty"
    );
    assertNotNull(
        user.getLastLogin(),
        "Last login should be initialized"
    );
    assertEquals(
        "",
        user.getLastLogin(),
        "Last login should be empty string initially"
    );
  }

  /**
   * {@code serviceCreatesFileWhenMissingTest} Ensures a new data file is
   * created when none exists and starts empty.
   */
  @Test
  public void serviceCreatesFileWhenMissingTest() throws IOException {
    Path tempDir = Files.createTempDirectory("tars-test");
    Path testFile = tempDir.resolve("new-users.json");
    TarsUserService service = new TarsUserService(testFile.toString(), null);
    assertTrue(Files.exists(testFile), "File should be created");
    List<TarsUser> users = service.listUsers();
    assertEquals(0, users.size(), "New file should have empty user list");
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code loadHandlesCorruptedFileTest} Confirms corrupt JSON input degrades
   * safely to empty list without exception.
   */
  @Test
  public void loadHandlesCorruptedFileTest() throws IOException {
    Path tempFile = Files.createTempFile("corrupt-users", ".json");
    Files.writeString(tempFile, "{ invalid json [[[");
    TarsUserService service = new TarsUserService(tempFile.toString(), null);
    List<TarsUser> users = service.listUsers();
    assertEquals(0, users.size(), "Corrupted file should return empty list");
    Files.deleteIfExists(tempFile);
  }

  /**
   * {@code ensureFileLogsInfoOnCreationTest} Verifies info log emitted upon
   * successful initialization of missing store.
   */
  @Test
  public void ensureFileLogsInfoOnCreationTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Path tempDir = Files.createTempDirectory("tars-log-test");
    Path testFile = tempDir.resolve("new-file.json");

    new TarsUserService(testFile.toString(), null);

    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Created user store at")),
        "Should log INFO when file created"
    );

    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code ensureFileLogsErrorOnIoExceptionTest} Simulates creation failure to
   * confirm error logging branch executes.
   */
  @Test
  public void ensureFileLogsErrorOnIoExceptionTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Path tempDir = Files.createTempDirectory("readonly-test");
    Path conflictPath = tempDir.resolve("conflict.json");
    Files.createDirectory(conflictPath); // Create as directory, not file
    
    new TarsUserService(conflictPath.toString(), null);

    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Failed to initialize user store")),
        "Should log ERROR when ensureFile fails"
    );

    logger.detachAppender(listAppender);
    Files.deleteIfExists(conflictPath);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code loadLogsErrorOnIoExceptionTest} Forces read failure to assert error
   * log emission and fallback behavior.
   */
  @Test
  public void loadLogsErrorOnIoExceptionTest() throws IOException {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    Path tempFile = Files.createTempFile("corrupt-for-log", ".json");
    Files.writeString(tempFile, "{ corrupt json }");

    new TarsUserService(tempFile.toString(), null);

    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Failed to read users file")),
        "Should log ERROR when load fails"
    );

    logger.detachAppender(listAppender);
    Files.deleteIfExists(tempFile);
  }

  /**
   * {@code persistSuccessPathTest} Confirms created user is durably written and
   * visible after service reload.
   */
  @Test
  public void persistSuccessPathTest() throws IOException {
    Path testDir = Files.createTempDirectory("persist-test");
    Path testFile = testDir.resolve("persist-users.json");
    
    // Copy classpath resource to temp file since service expects file path
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing: test-data-users.json");
      Files.copy(resourceStream, testFile);
    }
     
    TarsUserService service = new TarsUserService(testFile.toString(), null);
    TarsUser user = service.createUser(10L, "persist_test", "persist@test.com", "user");
    assertNotNull(user, "User should be created successfully");
    Long userId = user.getUserId();

    // Verify file contains new email before reload (diagnostic robustness)
    String immediateJson = Files.readString(testFile);
    assertTrue(
        immediateJson.contains("\"persist@test.com\""),
        "Persisted file should contain new user's email before reload"
    );
    assertTrue(
        immediateJson.contains("\"userId\""),
        "Persisted JSON should include userId fields"
    );
    ObjectMapper om = new ObjectMapper();
    List<TarsUser> parsed = om.readValue(
        immediateJson,
        new TypeReference<List<TarsUser>>() {}
    );
    assertTrue(
        parsed.stream().anyMatch(u -> userId.equals(u.getUserId())),
        "Parsed JSON should contain new user's userId=" + userId
    );
 
    TarsUserService reloadedService = new TarsUserService(testFile.toString(), null);
    TarsUser retrieved = reloadedService.findById(userId);
    assertNotNull(retrieved, "Persisted user should be retrievable after reload");
    assertEquals("persist_test", retrieved.getUsername(), "Persisted data should match");
    assertEquals("persist@test.com", retrieved.getEmail(), "Persisted email should match");

    assertTrue(
        service.listUsers().stream().anyMatch(u -> userId.equals(u.getUserId())),
        "In-memory list should contain newly created user id=" + userId
    );
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }

  /**
   * {@code persistCleansUpTempFileOnFailureTest} Ensures no leftover .tmp files
   * remain after multiple persists.
   */
  @Test
  public void persistCleansUpTempFileOnFailureTest() throws IOException {
    Path tempDir = Files.createTempDirectory("temp-cleanup-test");
    Path testFile = tempDir.resolve("users.json");
    
    TarsUserService service = new TarsUserService(testFile.toString(), null);
    
    for (int i = 0; i < 5; i++) {
      service.createUser(100L + i, "user" + i, "user" + i + "@test.com", "user");
    }
    
    long tmpFileCount = Files.list(tempDir)
        .filter(p -> p.getFileName().toString().endsWith(".tmp"))
        .count();
    
    assertEquals(
        0,
        tmpFileCount,
        "No temporary files should remain after successful persists"
    );
    
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(tempDir);
  }

  /**
   * {@code deactivateUserLogsWarnOnNotFoundTest} Validates warn log emitted when
   * deactivation targets missing id.
   */
  @Test
  public void deactivateUserLogsWarnOnNotFoundTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    userService.deactivateUser(999L);

    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("deactivateUser: user not found")),
        "Should log WARN when user not found"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code createUserLogsInfoOnSuccessTest} Confirms info log produced upon
   * successful user creation operation.
   */
  @Test
  public void createUserLogsInfoOnSuccessTest() {
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    userService.createUser(99L, "logtest", "log@test.com", "user");

    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.INFO
                && event.getFormattedMessage().contains("Created TarsUser")),
        "Should log INFO on user creation"
    );

    logger.detachAppender(listAppender);
  }

  /**
   * {@code persistAtomicMoveNotSupportedFallsBackTest} Verifies fallback to
   * non-atomic move when atomic operations are unsupported.
   */
  @Test
  public void persistAtomicMoveNotSupportedFallsBackTest() throws IOException {
    Path testDir = Files.createTempDirectory("atomic-fallback-test");
    Path testFile = testDir.resolve("users.json");
    
    // Mock FileMover that throws AtomicMoveNotSupportedException on first call
    final AtomicInteger moveCount = new AtomicInteger(0);
    final TarsUserService.FileMover mockMover = (source, target, options) -> {
      int count = moveCount.incrementAndGet();
      if (count == 1) {
        // First attempt with atomic flags
        throw new AtomicMoveNotSupportedException(
            source.toString(), 
            target.toString(), 
            "Atomic move not supported in test"
        );
      }
      // Second attempt succeeds (non-atomic fallback)
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    };
    
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing");
      Files.copy(resourceStream, testFile);
    }
    
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    
    TarsUserService service = new TarsUserService(testFile.toString(), mockMover);
    TarsUser created = service.createUser(200L, "atomic_test", "atomic@test.com", "user");
    assertNotNull(created, "User should be created");
    Long userId = created.getUserId();
    
    // Verify both move attempts occurred
    assertEquals(2, moveCount.get(), "Should attempt move twice");
    
    // Verify WARN log for atomic fallback
    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("Atomic move not supported")
                && event.getFormattedMessage().contains("non-atomic fallback")),
        "Should log WARN when atomic move fails"
    );
    
    // Verify data persisted successfully
    TarsUserService reloaded = new TarsUserService(testFile.toString(), null);
    TarsUser retrieved = reloaded.findById(userId);
    assertNotNull(retrieved, "User should persist after atomic fallback");
    assertEquals("atomic@test.com", retrieved.getEmail());
    
    // Verify no temp files remain
    long tmpCount = Files.list(testDir)
        .filter(p -> p.getFileName().toString().endsWith(".tmp"))
        .count();
    assertEquals(0, tmpCount, "Temp files should be cleaned up");
    
    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }

  /**
   * {@code persistFirstMoveFailsFallsBackTest} Validates recovery when first
   * move attempt fails with generic IOException.
   */
  @Test
  public void persistFirstMoveFailsFallsBackTest() throws IOException {
    Path testDir = Files.createTempDirectory("move-fallback-test");
    Path testFile = testDir.resolve("users.json");
    
    // Mock FileMover that fails first attempt, succeeds second
    final AtomicInteger moveCount = new AtomicInteger(0);
    final TarsUserService.FileMover mockMover = (source, target, options) -> {
      int count = moveCount.incrementAndGet();
      if (count == 1) {
        throw new IOException("Simulated first move failure");
      }
      Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    };
    
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing");
      Files.copy(resourceStream, testFile);
    }
    
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    
    TarsUserService service = new TarsUserService(testFile.toString(), mockMover);
    TarsUser created = service.createUser(201L, "fallback_test", "fallback@test.com", "user");
    assertNotNull(created, "User should be created");
    Long userId = created.getUserId();
    
    assertEquals(2, moveCount.get(), "Should attempt move twice");
    
    // Verify WARN log for first failure
    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("Atomic move failed")
                && event.getFormattedMessage().contains("non-atomic fallback succeeded")),
        "Should log WARN on first move failure"
    );
    
    // Verify successful persistence
    TarsUserService reloaded = new TarsUserService(testFile.toString(), null);
    TarsUser retrieved = reloaded.findById(userId);
    assertNotNull(retrieved, "User should persist after fallback");
    assertEquals("fallback@test.com", retrieved.getEmail());
    
    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }

  /**
   * {@code persistBothMovesFailTest} Confirms error logging and temp
   * cleanup occurs when all move attempts fail.
   */
  @Test
  public void persistBothMovesFailTest() throws IOException {
    Path testDir = Files.createTempDirectory("both-moves-fail-test");
    Path testFile = testDir.resolve("users.json");
    
    // Mock FileMover that fails both attempts
    final AtomicInteger moveCount = new AtomicInteger(0);
    final TarsUserService.FileMover mockMover = (source, target, options) -> {
      moveCount.incrementAndGet();
      throw new IOException("Simulated move failure");
    };
    
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing");
      Files.copy(resourceStream, testFile);
    }
    
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    
    TarsUserService service = new TarsUserService(testFile.toString(), mockMover);
    
    // Persist will fail but won't throw exception - it keeps in-memory state
    TarsUser created = service.createUser(202L, "fail_test", "fail@test.com", "user");
    assertNotNull(created, "User should be created in memory even if persist fails");
    
    assertEquals(2, moveCount.get(), "Should attempt move twice before giving up");
    
    // Verify ERROR log for move failure
    List<ILoggingEvent> logsList = listAppender.list;
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Persist move failed")),
        "Should log ERROR when both moves fail"
    );
    
    // Verify WARN log for persist not completing
    assertTrue(
        logsList.stream().anyMatch(event ->
            event.getLevel() == Level.WARN
                && event.getFormattedMessage().contains("Persist did not move new users file")),
        "Should log WARN when persist doesn't complete"
    );
    
    // Verify temp files cleaned up even on failure
    long tmpCount = Files.list(testDir)
        .filter(p -> p.getFileName().toString().endsWith(".tmp"))
        .count();
    assertEquals(0, tmpCount, "Temp files should be cleaned up even on failure");
    
    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }

  /**
   * {@code persistCreatesParentDirectoriesTest} Validates parent directory
   * creation when persist target path includes non-existent directories.
   */
  @Test
  public void persistCreatesParentDirectoriesTest() throws IOException {
    final Path testDir = Files.createTempDirectory("parent-dir-test");
    final Path nestedDir = testDir.resolve("level1").resolve("level2").resolve("level3");
    final Path testFile = nestedDir.resolve("users.json");
    
    // Parent directories don't exist yet
    assertFalse(Files.exists(nestedDir), "Nested directories should not exist yet");
    
    Logger logger = (Logger) LoggerFactory.getLogger(TarsUserService.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);
    
    TarsUserService service = new TarsUserService(testFile.toString(), null);
    TarsUser created = service.createUser(203L, "nested_test", "nested@test.com", "user");
    assertNotNull(created, "User should be created");
    final Long userId = created.getUserId();
    
    // Verify parent directories were created
    assertTrue(Files.exists(nestedDir), "Parent directories should be created");
    assertTrue(Files.isDirectory(nestedDir), "Parent path should be directory");
    
    // Verify data persisted
    assertTrue(Files.exists(testFile), "User file should exist");
    TarsUserService reloaded = new TarsUserService(testFile.toString(), null);
    TarsUser retrieved = reloaded.findById(userId);
    assertNotNull(retrieved, "User should be retrievable after parent creation");
    
    logger.detachAppender(listAppender);
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(nestedDir);
    Files.deleteIfExists(nestedDir.getParent());
    Files.deleteIfExists(nestedDir.getParent().getParent());
    Files.deleteIfExists(testDir);
  }

  /**
   * {@code persistDebugLogsOnSuccessTest} Confirms successful persist operation
   * completes without errors.
   */
  @Test
  public void persistDebugLogsOnSuccessTest() throws IOException {
    Path testDir = Files.createTempDirectory("debug-log-test");
    Path testFile = testDir.resolve("users.json");
    
    try (InputStream resourceStream = getClass()
        .getClassLoader()
        .getResourceAsStream("test-data/test-data-users.json")) {
      assertNotNull(resourceStream, "Fixture missing");
      Files.copy(resourceStream, testFile);
    }
    
    TarsUserService service = new TarsUserService(testFile.toString(), null);
    TarsUser created = service.createUser(204L, "debug_test", "debug@test.com", "user");
    assertNotNull(created, "User should be created");
    Long userId = created.getUserId();
    
    // Verify successful persistence by reloading
    TarsUserService reloaded = new TarsUserService(testFile.toString(), null);
    TarsUser retrieved = reloaded.findById(userId);
    assertNotNull(retrieved, "User should persist successfully");
    assertEquals("debug@test.com", retrieved.getEmail());
    
    Files.deleteIfExists(testFile);
    Files.deleteIfExists(testDir);
  }
}