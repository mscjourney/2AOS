package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.UserPreference;
import org.coms4156.tars.service.TarsService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the tests for the TarsService class.
 *
 * <p>Equivalence Partitions for TarsService Methods
 * ======== {@code List<UserPreference> getUserPreferenceList()} ========
 * Equivalence Partition 1: There are one or more User Preferences that have been set
 *    - Test Cases: getUserListTest
 * Equivalence Partition 2: No preferences have been set.
 *    - Test Cases: getUserListWithNoPreferencesAdded
 * ======== {@code boolean setUserPreference(UserPreference newUserPreference)} =========
 * Equivalence Partition 1: newUserPreference and id contained in newUserPreference is not null.
 *    There does NOT exist a preference associated with id
 * Equivalence Partition 2: newUserPreference and id contained in newUserPreference is not null.
 *    There does already exists a preference associated with id
 * Equivalence Partition 3: newUserPreference is not null. id contained in newUserPreference
 *    is null.
 * Equivalence Partiton 4: newUserPreference is null.
 *    - Test Cases for all setUserPreference partitions: setUserPreferenceTest
 * ======== {@code boolean clearPreference(Long userId)} ===========
 * Equivalence Partition 1: userId is non-null and non-negative.
 *    There exists a TarsUsers associated with id in newUserPreference
 * Equivalence Partition 2: userId is non-null and non-negative.
 *    There does NOT exist a TarsUsers associated with id in newUserPreference
 * Equivalence Partition 3: userId is non-null but is negative.
 * Equivalence Partition 4: userId is null.
 *    - Test Cases for all clearPreference partitions: testClearPreference
 * ======== {@code UserPreference getUserPreference(Long userId)} =========
* Equivalence Partition 1: userId is non-null and non-negative.
 *    There exists a TarsUsers associated with id in newUserPreference
 * Equivalence Partition 2: userId is non-null and non-negative.
 *    There does NOT exist a TarsUsers associated with id in newUserPreference
 * Equivalence Partition 3: userId is non-null but is negative.
 * Equivalence Partition 4: userId is null.
 *    - Test Cases For all getUserPreference partitions: getUserPreferenceTest
 */
@SpringBootTest
public class TarsServiceTest {
  
  private static String testFilePath;
  private static Path tempFile;
  public TarsService service;
  ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws IOException {
    tempFile = Files.createTempFile("test-userPreferences", ".json");
    testFilePath = tempFile.toString();

    Files.writeString(tempFile, "[]");
    service = new TarsService(testFilePath);

    UserPreference user1 
        = new UserPreference(1L, List.of("sunny"), List.of("12"), List.of("Boston"));
    UserPreference user2 = new UserPreference(2L, List.of("rainy"), List.of("11", "24"), 
                            List.of("New York", "Paris"));

    service.setUserPreference(user1);
    service.setUserPreference(user2);
  }
  
  @AfterAll
  static void cleanUp() throws IOException {
    Files.deleteIfExists(tempFile);
  }

  @Test
  void getUserListTest() {
    List<UserPreference> userList = service.getUserPreferenceList();
    UserPreference user = userList.get(0);
    assertEquals(user.getId(), 1);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("sunny");
    assertEquals(user.getWeatherPreferences(), weatherPreferences);
    
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("12");
    assertEquals(user.getTemperaturePreferences(), temperaturePreferences);

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Boston");
    assertEquals(user.getCityPreferences(), cityPreferences);
  }

  @Test
  void getUserPreferenceTest() {
    // Equivalence Partition 1
    UserPreference  user = service.getUserPreference(2L);
    assertEquals(user.getId(), 2);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("rainy");
    assertEquals(user.getWeatherPreferences(), weatherPreferences);
    
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("11");
    temperaturePreferences.add("24");
    assertEquals(user.getTemperaturePreferences(), temperaturePreferences);

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("Paris");
    assertEquals(user.getCityPreferences(), cityPreferences);
    
    // Equivalence Partition 2
    user = service.getUserPreference(0L);
    assertEquals(user, null);

    user = service.getUserPreference(10L);
    assertEquals(user, null);

    // Equivalence Partion 3 & 4
    // User Id cannot be negative or null
    assertNull(service.getUserPreference(-1L));
    assertNull(service.getUserPreference(null));
  }

  @Test
  void setUserPreferenceTest() {
    // Equivalence Partition 1
    UserPreference user1 
        = new UserPreference(1L, List.of("sunny"), List.of("12"), List.of("Boston"));
    assertEquals(service.getUserPreference(1L), user1);
    assertTrue(service.setUserPreference(user1)); // Modify an existing perference
    UserPreference ret1 = service.getUserPreference(1L);
    assertEquals(ret1.getCityPreferences(), List.of("Boston"));

    // Equivalence Partition 2
    user1.setCityPreferences(List.of("New York"));
    assertTrue(service.setUserPreference(user1));
    UserPreference ret2 = service.getUserPreference(1L);
    assertEquals(ret2.getCityPreferences(), List.of("New York"));

    // Equivalence Partition 3 & 4.
    UserPreference user0 = new UserPreference();
    assertFalse(service.setUserPreference(user0)); // Cannot add userPreference with id null.
    assertFalse(service.setUserPreference(null));
  }

  @Test
  void getUserListWithNoPreferencesAdded() {
    TarsService newService = new TarsService("");
    List<UserPreference> userList = newService.getUserPreferenceList();
    assertTrue(userList != null);
    assertEquals(userList.size(), 0);
  }

  @Test
  void testClearPreference() {
    // Equivalence Partition 1
    UserPreference user1 
        = new UserPreference(1L, List.of("sunny"), List.of("12"), List.of("Boston"));
    assertEquals(service.getUserPreference(1L), user1);
    assertTrue(service.clearPreference(1L));

    // Equivalence Partition 2
    UserPreference nullPreference = service.getUserPreference(1L);
    assertNull(nullPreference);
    
    assertFalse(service.clearPreference(1L));

    // Equivalence Partition 3 & 4
    // User Id cannot be negative or null
    assertFalse(service.clearPreference(-1L)); 
    assertFalse(service.clearPreference(null));
  }

  @Test
  public void testSetUserPreferenceWhenUsersIsNull() throws Exception {
    // Force private field `users` to null
    Field usersField = TarsService.class.getDeclaredField("users");
    usersField.setAccessible(true);
    usersField.set(service, null);
    assertNull(usersField.get(service));

    UserPreference pref = new UserPreference(1L, List.of("sunny"), List.of(), List.of());
    assertTrue(service.setUserPreference(pref));

    UserPreference retrieved = service.getUserPreference(1L);
    assertEquals(retrieved.getId(), 1);
    assertEquals(retrieved.getWeatherPreferences(), List.of("sunny"));
  }

  @Test
  public void testGetUserPreferenceWhenUsersIsNull() throws Exception {
    Field usersField = TarsService.class.getDeclaredField("users");
    usersField.setAccessible(true);
    usersField.set(service, null);
    assertNull(usersField.get(service));

    // userList should have been reloaded.
    UserPreference result = service.getUserPreference(1L);
    assertNotNull(result);
    assertEquals(result.getId(), 1);
    assertEquals(result.getWeatherPreferences(), List.of("sunny"));
    assertEquals(result.getTemperaturePreferences(), List.of("12"));
    assertEquals(result.getCityPreferences(), List.of("Boston"));

    UserPreference result2 = service.getUserPreference(2L);
    assertNotNull(result2);
    assertEquals(result2.getId(), 2);
    assertEquals(result2.getWeatherPreferences(), List.of("rainy"));
    assertEquals(result2.getTemperaturePreferences(), List.of("11", "24"));
    assertEquals(result2.getCityPreferences(), List.of("New York", "Paris"));
  }

  @Test
  public void testClearPreferenceWhenUsersIsNull() throws Exception {
    Field usersField = TarsService.class.getDeclaredField("users");
    usersField.setAccessible(true);
    usersField.set(service, null);
    assertNull(usersField.get(service));

    // userList should have been reloaded.
    assertTrue(service.clearPreference(1L));
    assertFalse(service.clearPreference(1L)); // Cannot clear preferences again
  }

  @Test
  public void testGetUserPreferenceListWhenUsersIsNull() throws Exception {
    Field usersField = TarsService.class.getDeclaredField("users");
    usersField.setAccessible(true);
    usersField.set(service, null);
    assertNull(usersField.get(service));

    // userList should have been reloaded.
    List<UserPreference> userList = service.getUserPreferenceList();
    assertEquals(userList.size(), 2);
  }

  @Test
  public void testSaveDataThrowsException() throws Exception {
    // Create a spy of your service
    TarsService spyService = Mockito.spy(service);

    // Mock ObjectMapper to throw IOException
    ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
    Mockito.doThrow(new IOException("disk full")).when(mockMapper)
           .writeValue(Mockito.any(File.class), Mockito.any());

    // Use reflection to inject the mock mapper
    Field mapperField = TarsService.class.getDeclaredField("mapper");
    mapperField.setAccessible(true);
    mapperField.set(spyService, mockMapper);

    // Save the current userlist to file
    spyService.setUserPreference(new UserPreference(3L));

    File testFile = new File(testFilePath);
    List<UserPreference> fileContents = mapper.readValue(testFile, List.class);
    assertEquals(fileContents.size(), 2);
  }
}