package org.coms4156.tars.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.service.TarsUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@code UserServiceTest} Unit tests for UserService.
 */
public class TarsUserServiceTest {

  private TarsUserService userService = new TarsUserService(String.format(
      "%sTeamProject/data/test-data/test-data-users.json",
      System.getProperty("user.dir")));

  // Reset the user service before each test
  @BeforeEach
  void setUp() {
    userService = new TarsUserService(String.format(
        "%sTeamProject/data/test-data/test-data-users.json",
        System.getProperty("user.dir")));
  }

  // Is userService initialized properly
  @Test
  void userServiceInitializationTest() {
    assertNotNull(userService, "UserService should be initialized");
  }
    
}
