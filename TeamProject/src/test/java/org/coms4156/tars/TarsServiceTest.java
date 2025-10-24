package org.coms4156.tars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.User;
import org.coms4156.tars.service.TarsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the tests for the TarsService class.
 */
@SpringBootTest
public class TarsServiceTest {
  
  public TarsService service;

  @BeforeEach
  void setUp() {
    service = new TarsService("");

    User user1 = new User(1, 1, List.of("sunny"), List.of("70F"), List.of("Boston"));
    User user2 = new User(2, 2, List.of("rainy"), List.of("60F", "67F"), 
                            List.of("New York", "Paris"));

    service.addUser(user1);
    service.addUser(user2);
  }

  @Test
  void getUserListTest() {
    List<User> userList = service.getUserList();
    User user = userList.get(0);
    assertEquals(user.getId(), 1);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("sunny");
    assertEquals(user.getWeatherPreferences(), weatherPreferences);
    
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("70F");
    assertEquals(user.getTemperaturePreferences(), temperaturePreferences);

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Boston");
    assertEquals(user.getCityPreferences(), cityPreferences);
  }

  @Test
  void getUserTest() {
    User user = service.getUser(0);
    assertEquals(user, null);

    user = service.getUser(2);

    assertEquals(user.getId(), 2);

    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("rainy");
    assertEquals(user.getWeatherPreferences(), weatherPreferences);
    
    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("60F");
    temperaturePreferences.add("67F");
    assertEquals(user.getTemperaturePreferences(), temperaturePreferences);

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("New York");
    cityPreferences.add("Paris");
    assertEquals(user.getCityPreferences(), cityPreferences);

  }

  @Test
  void addUserTest() {
    User user1 = new User(1, 1, List.of("sunny"), List.of("70F"), List.of("Boston"));

    assertEquals(service.getUser(1), user1);
    assertFalse(service.addUser(user1)); // Duplicate User

    User user0 = new User();
    assertTrue(service.addUser(user0));
    assertFalse(service.addUser(user0));

    assertFalse(service.addUser(null));
  }
}