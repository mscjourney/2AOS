package org.coms4156.tars.controller;

import java.util.List;
import java.util.Map;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherAlertModel;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.model.WeatherRecommendation;
import org.coms4156.tars.service.ClientService;
import org.coms4156.tars.service.TarsService;
import org.coms4156.tars.service.TarsUserService;
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
  private final ClientService clientService;
  private final TarsUserService tarsUserService;

  /**
   * Constructor for {@code RouteController}.
   *
   * @param tarsService The TarsService instance.
   * @param clientService The ClientService instance.
   * @param tarsUserService The TarsUserService instance.
   */
  public RouteController(
      TarsService tarsService, ClientService clientService, TarsUserService tarsUserService) {
    this.tarsService = tarsService;
    this.clientService = clientService;
    this.tarsUserService = tarsUserService;
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
   * @param body JSON object containing the new client's name and email.
   *
   * @return A ResponseEntity indicating the result of the operation.
   */
  @PostMapping({"/client/create"})
  public ResponseEntity<?> createClient(@RequestBody Map<String, String> body) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/create invoked");
    }

    // Validate request body
    if (body == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: request body is null");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'name' and 'email'.");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("POST /client/create raw body keys={}", body.keySet());
    }

    // Validate name
    if (!body.containsKey("name")) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: 'name' field missing");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'name'.");
    }
    String name = body.get("name") == null ? "" : body.get("name").trim();
    if (name.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: blank name provided");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client name cannot be blank.");
    }

    // Validate email
    if (!body.containsKey("email")) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: 'email' field missing");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'email'.");
    }
    String email = body.get("email") == null ? "" : body.get("email").trim();
    if (email.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: blank email provided");
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Client email cannot be blank.");
    }

    // Check name uniqueness
    if (!clientService.uniqueNameCheck(name)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create conflict: name '{}' already exists", name);
      }
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Client name already exists.");
    }

    // Check email uniqueness
    if (!clientService.uniqueEmailCheck(email)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create conflict: email '{}' already exists", email);
      }
      return ResponseEntity.status(HttpStatus.CONFLICT).body("Client email already exists.");
    }
    
    Client created = clientService.createClient(name, email);
    if (created == null) {
      if (logger.isErrorEnabled()) {
        logger.error("POST /client/create internal error: client creation returned null name='{}'",
            name);
      }
      return ResponseEntity.status(
        HttpStatus.INTERNAL_SERVER_ERROR
        ).body("Failed to create client.");
    }
    
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/create success: clientId={} (view key in admin portal)", 
          created.getClientId());
    }

    Map<String, Object> response = Map.of(
        "clientId", created.getClientId(),
        "name", created.getName(),
        "message",
        "Client created successfully. "
         + "Log in to the admin portal to retrieve your API key.",
        "portalUrl",
          "https://admin.tars.example.com/clients/"
          + created.getClientId()
          + "/credentials"
    );
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Handles POST requests to create a user for a specific client.
   * Pass new user information in the request body as a JSON object.
   *
   * @param newUserRequestBody JSON object containing new user information.
   *
   * @return A resource indicating that the user was created.
  */
  @PostMapping({"/client/createUser"})
  public ResponseEntity<?> createClientUser(@RequestBody TarsUser newUserRequestBody) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/createUser invoked");
    }
    if (newUserRequestBody == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/createUser failed: body null");
      }
      return ResponseEntity.badRequest().body("Body cannot be null.");
    }

    // Validate clientId
    Long clientId = newUserRequestBody.getClientId();
    if (clientId == null || clientId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: invalid clientId={}",
            clientId);
      }
      return ResponseEntity.badRequest().body("Invalid clientId.");
    }

    // Validate username
    String username = newUserRequestBody.getUsername() == null
        ? ""
        : newUserRequestBody.getUsername().trim();
    if (username.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: blank username clientId={}",
            clientId);
      }
      return ResponseEntity.badRequest().body("Username cannot be blank.");
    }

    // validate email
    String userEmail = newUserRequestBody.getUserEmail() == null
        ? ""
        : newUserRequestBody.getUserEmail().trim();
    if (userEmail.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: blank userEmail username='{}' clientId={}",
            username, clientId);
      }
      return ResponseEntity.badRequest().body("Email cannot be blank.");
    }

    // Validate role
    String role = newUserRequestBody.getRole() == null ? "" : newUserRequestBody.getRole().trim();
    if (role.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: blank role username='{}' clientId={}",
            username, clientId);
      }
      return ResponseEntity.badRequest().body("Role cannot be blank.");
    }

    // Log validation success
    if (logger.isDebugEnabled()) {
      logger.debug(
          "POST /client/createUser validation passed clientId={} username='{}' role='{}'",
          clientId, username, role);
    }
    
    // Check if client exists
    Client client = clientService.getClient(clientId.intValue());
    if (client == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/createUser failed: client not found clientId={}", clientId);
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client not found.");
    }

    // Check username uniqueness
    if (tarsUserService.existsByClientIdAndUsername(clientId, username)) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser conflict: a user "
            + "with username='{}' exists for clientId={}",
            username, clientId);
      }
      return ResponseEntity.status(
        HttpStatus.CONFLICT).body(
          "Username already exists for this client."
          );
    }

    // Check email uniqueness
    if (tarsUserService.existsByClientIdAndUserEmail(clientId, userEmail)) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser conflict: userEmail='{}' exists for clientId={}",
            userEmail, clientId);
      }
      return ResponseEntity.status(
        HttpStatus.CONFLICT).body(
          "A user with the email already exists for this client."
          );
    }

    TarsUser created = tarsUserService.createUser(clientId, username, userEmail, role);
    if (created == null) {
      if (logger.isErrorEnabled()) {
        logger.error(
            "POST /client/createUser internal error after create "
            + "clientId={} username='{}' usserEmail='{}' role='{}'",
            clientId, username);
      }
      return ResponseEntity.status(
        HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create user.");
    }
    if (logger.isInfoEnabled()) {
      logger.info(
          "POST /client/createUser success: newUserId={} clientId={} username='{}'",
          created.getUserId(), clientId, username);
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "POST /client/createUser created user detail active={} role='{}'",
          created.getActive(), created.getRole());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
        logger.info(
            "Weather alert retrieved for location city={} lat={} lon={}",
            city, lat, lon);
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
        logger.error(
            "Error retrieving weather alerts city={} lat={} lon={}",
            city, lat, lon, e);
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
}
