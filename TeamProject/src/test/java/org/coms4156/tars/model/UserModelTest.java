package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
  public UserPreference user;

  @BeforeEach
  public void setUpUserForTesting() {
    user = new UserPreference(1L);
  }

  @Test
  void testUserId() {
    assertEquals(user.getId(), 1);

    user = new UserPreference();
    assertEquals(user.getId(), null);

    user = new UserPreference(10L);
    assertEquals(user.getId(), 10);
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
    UserPreference newUser = new UserPreference(1L);
    assertTrue(user.equals(newUser));

    newUser = new UserPreference(2L);
    assertFalse(user.equals(newUser));

    newUser = new UserPreference();
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
    UserPreference user1 = new UserPreference(1L);
    UserPreference user2 = new UserPreference(1L);
    assertEquals(user1.hashCode(), user2.hashCode());
    
    UserPreference user3 = new UserPreference(2L);
    assertTrue(user1.hashCode() != user3.hashCode() || !user1.getId().equals(user3.getId()));

    UserPreference user0 = new UserPreference();
    assertEquals(user0.hashCode(), 0);
  }

  @Test
  void printUser() {
    UserPreference user1 = new UserPreference(1L);
    assertEquals(user1.toString(), 
        "UserPreference{id: 1, weatherPreferences: [], "
        + "temperaturePreferences: [], cityPreferences: []}");
  }
  
  @Test 
  void testUserInitializationFull() {
    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Syndey");
    cityPreferences.add("London");

    List<String> tempPreferences = new ArrayList<>();
    tempPreferences.add("82F");

    user = new UserPreference(1L, new ArrayList<>(), tempPreferences, cityPreferences);
    
    assertEquals(user.getId(), 1);
    assertEquals(user.getWeatherPreferences(), new ArrayList<>());
    assertEquals(user.getTemperaturePreferences(), tempPreferences);
    assertEquals(user.getCityPreferences(), cityPreferences);
  }
}