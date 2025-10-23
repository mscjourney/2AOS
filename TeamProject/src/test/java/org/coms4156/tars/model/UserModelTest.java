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
public class UserModelTest {
  public User user;

  @BeforeEach
  public void setUpUserForTesting() {
    user = new User(1);
  }

  @Test
  void testUserId() {
    assertEquals(user.getId(), 1);

    user = new User();
    assertEquals(user.getId(), 0);

    user = new User(10);
    assertEquals(user.getId(), 10);

    assertThrows(IllegalArgumentException.class, () -> new User(-1));
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
    User newUser = new User(1);
    assertTrue(user.equals(newUser));

    newUser = new User(2);
    assertFalse(user.equals(newUser));

    newUser = new User();
    assertFalse(user.equals(newUser));
  }
}