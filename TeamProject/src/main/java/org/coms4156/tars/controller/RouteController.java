package org.coms4156.tars.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.ArrayList;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TravelAdvisory;
import org.coms4156.tars.model.TravelAdvisoryModel;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherAlertModel;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.model.WeatherRecommendation;
import org.coms4156.tars.service.TarsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * This class defines an API that accesses endpoints.
 */
@RestController
public class RouteController {

  private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

  private final TarsService tarsService;

  public RouteController(TarsService tarsService) {
    this.tarsService = tarsService;
  }

  /**
   * The index route of the API.
   * Request Method: GET
   * Returns a welcome message.
   *
   * @return A welcome string message.
   */
  @GetMapping({"/", "/index"})
  public String index() {
    if (logger.isInfoEnabled()) {
      logger.info("GET /index invoked");
    }
    return "Welcome to the TARS Home Page!";
  }

  /**
   * An endpoint to create a new client.
   * Request Method: POST
   * Returns a new client resource.
   *
   * @param client The client object to be created.
   * @return A ResponseEntity indicating the result of the operation.
   */
  @PostMapping({"/clients"})
  public ResponseEntity<String> createClientRoute(@RequestBody User client) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /clients invoked (not implemented). Incoming body userId={}", 
          client != null ? client.getId() : "null");
    }
    return new ResponseEntity<>(
      "createClientRoute is not yet implemented.",
      HttpStatus.NOT_IMPLEMENTED
    );
  }

  /**
   * Handles POST requests to create a user for a specific client.
   *
   * @param clientId The ID of the client.
   * @return A ResponseEntity indicating the result of the operation.
  */
  @PostMapping({"/clients/{clientId}/newUser"})
  public ResponseEntity<String> addClientUser(@PathVariable String clientId) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /clients/{}/newUser invoked (not implemented)", clientId);
    }
    return new ResponseEntity<>(
      "addClientUser is not yet implemented.",
      HttpStatus.NOT_IMPLEMENTED
    );
  }

  /**
   * Handles PUT requests to add a new user's preferences.
   *
   * @param id the id of the user that we are adding
   * @param user the User object that contains the different preferences of the user.
   * @return a ResponseEntity containing the User Preferences data in json format if successful,
   *          or an error message indicating that the user id already exists.
   */
  @PutMapping({"/user/{id}/add"})
  public ResponseEntity<?> addUser(@PathVariable int id, @RequestBody User user) {
    if (logger.isInfoEnabled()) {
      logger.info("PUT /user/{}/add invoked", id);
    }
    if (user == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("PUT /user/{}/add failed: request body is null", id);
      }
      return new ResponseEntity<>("User body cannot be null.", HttpStatus.BAD_REQUEST);
    }
    boolean added = tarsService.addUser(user);
    if (added) {
      if (logger.isInfoEnabled()) {
        logger.info("User added successfully id={}", id);
      }
      return new ResponseEntity<>(user, HttpStatus.OK);
    } else {
      if (logger.isWarnEnabled()) {
        logger.warn("User add conflict id={}: already exists", id);
      }
      return new ResponseEntity<>("User Id already exists.", HttpStatus.CONFLICT);
    }
  }

  /**
   * Handles PUT requests to remove a user.
   *
   * @param id the id of the user that we are removing
   * @return a ResponseEntity containing the message stating whether the user removal was
   *          successful or not.
   */
  @PutMapping({"/user/{id}/remove"})
  public ResponseEntity<?> removeUser(@PathVariable int id) {
    if (logger.isInfoEnabled()) {
      logger.info("PUT /user/{}/remove invoked", id);
    }
    if (id < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("PUT /user/{}/remove failed: User cannot have negative ids", id);
      }
      return new ResponseEntity<>("User Id cannot be negative.", HttpStatus.BAD_REQUEST);
    }
    boolean removed = tarsService.removeUser(id);
    if (removed) {
      if (logger.isInfoEnabled()) {
        logger.info("User removed successfully id={}", id);
      }
      return new ResponseEntity<>("User removed successfully.", HttpStatus.OK);
    }
    if (logger.isWarnEnabled()) {
      logger.warn("User remove failed");
    }
    return new ResponseEntity<>("User removed failed.", HttpStatus.CONFLICT);
  }

  /**
   * Handles GET requests to retrieve a user's preferences.
   *
   * @param id the id of the user that we are retrieving
   * @return a ResponseEntity containing the User Preferences data in json format if succesful,
   *           or an error message indicating that there are no users with the given id.
   */
  @GetMapping({"/user/{id}"})
  public ResponseEntity<?> getUser(@PathVariable int id) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /user/{} invoked", id);
    }
    for (User user : tarsService.getUserList()) {
      if (user.getId() == id) {
        if (logger.isInfoEnabled()) {
          logger.info("GET /user/{} success", id);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
      }
    }
    if (logger.isWarnEnabled()) {
      logger.warn("GET /user/{} not found", id);
    }
    return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Handles GET requests to retrieve information about all the existing users.
   *
   * @return a ResponseEntity containing the User Preferences data in json format for all users
   *          if successful. Otherwise, return the status code INTERNAL_SERVER_ERROR. 
   */
  @GetMapping("/userList")
  public ResponseEntity<List<User>> getUserList() {
    try {
      List<User> userList = tarsService.getUserList();
      return new ResponseEntity<>(userList, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/userList/client/{clientId}")
  public ResponseEntity<List<User>> getClientUserList(@PathVariable int clientId) {
    try {
      List<User> userList = tarsService.getUserList();
      List<User> clientUserList = new ArrayList<>();
      for (User user : userList) {
        if (user.getClientId() == clientId) {
          clientUserList.add(user);
        }
      }
      return new ResponseEntity<>(clientUserList, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather recommendations for a specified city
   * and number of forecast days.
   */
  @GetMapping("/recommendation/weather/")
  public ResponseEntity<WeatherRecommendation> getWeatherRecommendation(
      @RequestParam String city,
      @RequestParam int days) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /recommendation/weather city={} days={}", city, days);
    }
    try {
      if (days <= 0 || days > 14) {
        if (logger.isWarnEnabled()) {
          logger.warn("Invalid days parameter: {}", days);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(city, days);

      if (logger.isInfoEnabled()) {
        logger.info("Weather recommendation generated for city={} days={}", city, days);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Recommendation detail: {}", recommendation);
      }
      return ResponseEntity.ok(recommendation);

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error generating recommendation city={} days={}", city, days, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather alerts for a specified location.
   */
  @GetMapping("/alert/weather")
  public ResponseEntity<WeatherAlert> getWeatherAlerts(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lon) {

    if (logger.isInfoEnabled()) {
      logger.info("GET /alert/weather city={} lat={} lon={}", city, lat, lon);
    }

    try {
      if (city == null && (lat == null || lon == null)) {
        if (logger.isWarnEnabled()) {
          logger.warn("Missing location parameters for alert request");
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(city, lat, lon);

      if (logger.isInfoEnabled()) {
        logger.info("Weather alert retrieved for location city={} lat={} lon={}", city, lat, lon);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Alert detail: {}", alert);
      }
      return ResponseEntity.ok(alert);

    } catch (IllegalArgumentException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid argument for weather alerts city={} lat={} lon={}: {}", 
            city, lat, lon, e.getMessage());
      }
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error retrieving weather alerts city={} lat={} lon={}", city, lat, lon, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather alerts for the specified user
   * based on their city preferences.
   */
  @GetMapping("/alert/weather/user/{userId}")
  public ResponseEntity<?> getUserWeatherAlerts(@PathVariable int userId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /alert/weather/user/{} invoked", userId);
    }
    try {
      User user = tarsService.getUser(userId);
      if (userId < 0) {
        if (logger.isWarnEnabled()) {
          logger.warn("Negative userId provided: {}", userId);
        }
        return new ResponseEntity<>("User Id cannot be less than zero.", HttpStatus.BAD_REQUEST);
      }

      if (user == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("No user found for id={}", userId);
        }
        return new ResponseEntity<>("No such user.", HttpStatus.NOT_FOUND);
      }

      List<WeatherAlert> alertList = WeatherAlertModel.getUserAlerts(user);
      if (logger.isInfoEnabled()) {
        logger.info("Retrieved {} alerts for userId={}", alertList.size(), userId);
      }
      return ResponseEntity.ok(alertList);

    } catch (IllegalArgumentException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("Bad request for user weather alerts userId={}: {}", userId, e.getMessage());
      }
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error retrieving user alerts userId={}", userId, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve crime summary data.
   */
  @GetMapping("/crime/summary")
  public ResponseEntity<?> getCrimeSummary(
      @RequestParam String state,
      @RequestParam String offense,
      @RequestParam String month,
      @RequestParam String year) {

    if (logger.isInfoEnabled()) {
      logger.info("GET /crime/summary state={} offense={} month={} year={}", 
          state, offense, month, year);
    }

    try {
      CrimeModel model = new CrimeModel();
      String result = model.getCrimeSummary(state, offense, month, year);

      if (logger.isDebugEnabled()) {
        logger.debug("Raw crime summary API result state={} offense={} month={} year={}: {}", 
            state, offense, month, year, result);
      }

      CrimeSummary summary = new CrimeSummary(
          state,
          month,
          year,
          "Fetched crime data successfully for " + offense + " : " + result
      );

      if (logger.isInfoEnabled()) {
        logger.info("Crime summary constructed state={} offense={}", state, offense);
      }
      return ResponseEntity.ok(summary);

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error fetching crime summary state={} offense={} month={} year={}", 
            state, offense, month, year, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve a travel advisory for a given country.
   * Example: GET /country/Algeria
   */
  @GetMapping("/country/{country}")
  public ResponseEntity<?> getCountryAdvisory(@PathVariable String country) {
    logger.info("GET /country/{} invoked", country);

    try {
      TravelAdvisoryModel model = new TravelAdvisoryModel();
      TravelAdvisory advisory = model.getTravelAdvisory(country);

      if (advisory == null) {
        logger.warn("No advisory found for country={}", country);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

      return ResponseEntity.ok(advisory.toString());

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());

    } catch (Exception e) {
      logger.error("Error retrieving advisory for country={}", country, e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}
