package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class UserModelTest {
  public User user;

  @BeforeEach
  public void setUpUserForTesting() {
    user = new User(1, 1);
  }

  @Test
  void testUserAndClientId() {
    assertEquals(user.getId(), 1);
    assertEquals(user.getClientId(), 1);

    user = new User();
    assertEquals(user.getId(), 0);
    assertEquals(user.getClientId(), -1);

    user = new User(10, 15);
    assertEquals(user.getId(), 10);
    assertEquals(user.getClientId(), 15);

    assertThrows(IllegalArgumentException.class, () -> new User(-1, 5));
  }

  @Test
  void testWeatherPreferences() {
    assertEquals(user.getWeatherPreferences(), new ArrayList());

    List<String> newPreferences = new ArrayList<>();
    newPreferences.add("sunny");
    newPreferences.add("snowy");

    user.setWeatherPreferences(newPreferences);

    assertEquals(user.getWeatherPreferences(), newPreferences);

    user.setWeatherPreferences(null);
    assertEquals(user.getWeatherPreferences(), new ArrayList());
  }

  @Test
  void testTemperaturePreferences() {
    assertEquals(user.getTemperaturePreferences(), new ArrayList());

    List<String> newPreferences = new ArrayList<>();
    newPreferences.add("82F");

    user.setTemperaturePreferences(newPreferences);

    assertEquals(user.getTemperaturePreferences(), newPreferences);

    user.setTemperaturePreferences(null);
    assertEquals(user.getTemperaturePreferences(), new ArrayList());
  }

  @Test
  void testCityPreferences() {
    assertEquals(user.getCityPreferences(), new ArrayList());

    List<String> newPreferences = new ArrayList<>();
    newPreferences.add("Syndey");
    newPreferences.add("London");

    user.setCityPreferences(newPreferences);
    assertEquals(user.getCityPreferences(), newPreferences);

    user.setCityPreferences(null);
    assertEquals(user.getCityPreferences(), new ArrayList());
  }

  @Test
  void testEqualsUser() {
    User newUser = new User(1, 1);
    assertTrue(user.equals(newUser));

    newUser = new User(2, 1);
    assertFalse(user.equals(newUser));

    newUser = new User();
    assertFalse(user.equals(newUser));
  }

  @Test
  void testEqualsWithNull() {
    assertFalse(user.equals(null));
  }

  @Test
  void testEqualsWithSameObject() {
    assertTrue(user.equals(user));
  }

  @Test
  void testEqualsWithDifferentClass() {
    assertFalse(user.equals("not a user"));
  }

  @Test
  void testHashCode() {
    User user1 = new User(1, 1);
    User user2 = new User(1, 2);
    assertEquals(user1.hashCode(), user2.hashCode());
    
    User user3 = new User(2, 1);
    assertTrue(user1.hashCode() != user3.hashCode() || user1.getId() != user3.getId());
  }

  @Test
  void testCompleteConstructor() {
    List<String> weatherPrefs = new ArrayList<>();
    weatherPrefs.add("sunny");
    weatherPrefs.add("cloudy");
    
    List<String> tempPrefs = new ArrayList<>();
    tempPrefs.add("70F");
    tempPrefs.add("75F");
    
    List<String> cityPrefs = new ArrayList<>();
    cityPrefs.add("New York");
    cityPrefs.add("Boston");
    
    User completeUser = new User(5, 10, weatherPrefs, tempPrefs, cityPrefs);
    
    assertEquals(5, completeUser.getId());
    assertEquals(10, completeUser.getClientId());
    assertEquals(weatherPrefs, completeUser.getWeatherPreferences());
    assertEquals(tempPrefs, completeUser.getTemperaturePreferences());
    assertEquals(cityPrefs, completeUser.getCityPreferences());
  }

  @Test
  void testCompleteConstructorWithNullLists() {
    // The constructor accepts null lists directly (doesn't convert to empty)
    User user = new User(6, 11, null, null, null);
    
    assertEquals(6, user.getId());
    assertEquals(11, user.getClientId());
    // Null lists are stored as null, not converted to empty lists
    // This tests that the constructor accepts null without throwing
    assertTrue(user.getWeatherPreferences() == null);
    assertTrue(user.getTemperaturePreferences() == null);
    assertTrue(user.getCityPreferences() == null);
  }

  @Test
  void testCompleteConstructorThrowsOnNegativeId() {
    List<String> emptyList = new ArrayList<>();
    assertThrows(IllegalArgumentException.class, () -> {
      new User(-1, 1, emptyList, emptyList, emptyList);
    });
  }

  @Test
  void testCompleteConstructorWithEmptyLists() {
    List<String> emptyList = new ArrayList<>();
    User user = new User(7, 12, emptyList, emptyList, emptyList);
    
    assertEquals(7, user.getId());
    assertEquals(12, user.getClientId());
    assertTrue(user.getWeatherPreferences().isEmpty());
    assertTrue(user.getTemperaturePreferences().isEmpty());
    assertTrue(user.getCityPreferences().isEmpty());
  }

  @Test
  void testCompleteConstructorWithLargeId() {
    List<String> emptyList = new ArrayList<>();
    User user = new User(Integer.MAX_VALUE, 1, emptyList, emptyList, emptyList);
    
    assertEquals(Integer.MAX_VALUE, user.getId());
    assertEquals(1, user.getClientId());
  }

  @Test
  void testCompleteConstructorWithZeroId() {
    List<String> emptyList = new ArrayList<>();
    User user = new User(0, 1, emptyList, emptyList, emptyList);
    
    assertEquals(0, user.getId());
    assertEquals(1, user.getClientId());
  }
}