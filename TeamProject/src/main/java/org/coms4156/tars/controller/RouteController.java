package org.coms4156.tars.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.coms4156.tars.model.CitySummary;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.TravelAdvisory;
import org.coms4156.tars.model.TravelAdvisoryModel;
import org.coms4156.tars.model.UserPreference;
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
import org.springframework.web.bind.annotation.DeleteMapping;
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
  private static final String EMAIL_REGEX =
      "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

  private final TarsService tarsService;
  private final ClientService clientService;
  private final TarsUserService tarsUserService;

  /**
   * {@code isValidEmai} Validates the format of an email address.
   *
   * @param email The email address to validate.
   * @return true if the email format is valid, false otherwise.
   */
  private boolean isValidEmail(String email) {
    return email != null && email.matches(EMAIL_REGEX);
  }

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
  public ResponseEntity<?> createClient(@RequestBody(required = false) Map<String, String> body) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/create invoked");
    }

    // Validate request body
    if (body == null || body.isEmpty()) {
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
    if (!isValidEmail(email)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: invalid email format '{}'", email);
      }
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format.");
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
  public ResponseEntity<?> createClientUser(
      @RequestBody(required = false) TarsUser newUserRequestBody) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/createUser invoked with body keys={}",
          newUserRequestBody == null
              ? "null"
              : newUserRequestBody.getClass().getDeclaredFields());
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

    // validate user email
    String email = newUserRequestBody.getEmail() == null
        ? ""
        : newUserRequestBody.getEmail().trim();
    if (email.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: blank email username='{}' clientId={}",
            username, clientId);
      }
      return ResponseEntity.badRequest().body("Email cannot be blank.");
    }
    if (!isValidEmail(email)) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser failed: invalid email format '{}' clientId={}",
            email, clientId);
      }
      return ResponseEntity.badRequest().body("Invalid email format.");
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
    if (tarsUserService.existsByClientIdAndUserEmail(clientId, email)) {
      if (logger.isWarnEnabled()) {
        logger.warn(
            "POST /client/createUser conflict: email='{}' exists for clientId={}",
            email, clientId);
      }
      return ResponseEntity.status(
        HttpStatus.CONFLICT).body(
          "A user with the email already exists for this client."
          );
    }

    TarsUser created = tarsUserService.createUser(clientId, username, email, role);
    if (created == null) {
      if (logger.isErrorEnabled()) {
        logger.error(
            "POST /client/createUser internal error after create "
            + "clientId={} username='{}' userEmail='{}' role='{}'",
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
   * Handles PUT requests to set a user's preferences.
   *
   * @param id the id of the user that we are adding
   * @param userPreference the UserPreference object that contains the different 
   *                       preferences of the user.
   * @return a ResponseEntity containing the User Preferences data in json format if successful,
   *          or an error message indicating that the user argument was invalid.
   */
  @PutMapping({"/setPreference/{id}"})
  public ResponseEntity<?> setUserPreference(@PathVariable Long id, 
                                              @RequestBody UserPreference userPreference) {  
    if (logger.isInfoEnabled()) {
      logger.info("PUT /setPreference/{} invoked", id);
    }

    // If id was null, entry point would default to 404. No need to check
    if (id < 0) { // User Id cannot be negative
      if (logger.isWarnEnabled()) {
        logger.warn("TarsUser with id={} does not exist.", id);
      }
      return new ResponseEntity<>("User Id cannot be negative.", HttpStatus.BAD_REQUEST);
    }

    if (!id.equals(userPreference.getId())) {
      if (logger.isWarnEnabled()) {
        logger.warn("RequestBody userId {} does not match path variable id {}.", 
                        userPreference.getId(), id);
      }
      return new ResponseEntity<>("Path Variable and RequestBody User Id do not match.", 
                                    HttpStatus.BAD_REQUEST);
    }

    // Checks that a corresponding TarsUser with specified id already exists.
    TarsUser user = tarsUserService.findById(id);
    if (user == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("TarsUser with id={} does not exist.", id);
      }
      return new ResponseEntity<>("TarsUser not found.", HttpStatus.NOT_FOUND);
    }

    boolean added = tarsService.setUserPreference(userPreference);
    if (added) {
      if (logger.isInfoEnabled()) {
        logger.info("User Preference set successfully id={} ", id);
      }
      return new ResponseEntity<>(userPreference, HttpStatus.OK);
    } else {
      if (logger.isWarnEnabled()) {
        logger.warn("Invalid Argument. User Body was null");
      }
      return new ResponseEntity<>("Invalid Argument. User Body was null.", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Handles PUT requests to remove a user.
   *
   * @param id the id of the user that we are removing
   * @return a ResponseEntity containing a success message if the user was successfully removed,
   *          or an error message indicating that the user was not found or the id is invalid.
   */
  @PutMapping({"/clearPreference/{id}"})
  public ResponseEntity<?> clearUserPreference(@PathVariable Long id) {    
    if (logger.isInfoEnabled()) {
      logger.info("PUT /clearPreference/{} invoked", id);
    }
    
    if (id < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("PUT /clearPreference/{} failed: User cannot have negative ids", id);
      }
      return new ResponseEntity<>("User Id cannot be negative.", HttpStatus.BAD_REQUEST);
    }

    // Checks that a corresponding TarsUser with specified id already exists.
    TarsUser tarsUser = tarsUserService.findById(id);
    // If id was null, entry point would default to 404.
    if (tarsUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("TarsUser with id={} does not exist.", id);
      }
      return new ResponseEntity<>("TarsUser not found.", HttpStatus.NOT_FOUND);
    }

    // Id cannot be negative at this point. Still checks again in clearPreference.
    boolean removed = tarsService.clearPreference(id);
    if (removed) { // UserPreferences successfully cleared
      if (logger.isInfoEnabled()) {
        logger.info("User Preference cleared successfully id={}", id);
      }
      return new ResponseEntity<>("User Preference cleared successfully.", HttpStatus.OK); 
    } else { // UserPreferences did not exist.
      if (logger.isWarnEnabled()) {
        logger.warn("User remove failed");
      }
      return new ResponseEntity<>("User had no existing preferences.", HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * Handles GET requests to retrieve a user's preferences.
   *
   * @param id the id of the user that we are retrieving preferences for.
   * @return a ResponseEntity containing the UserPreference data in json format if succesful,
   *           or an error message indicating that there are no users with the given id.
   */
  @GetMapping({"/retrievePreference/{id}"})
  public ResponseEntity<?> getUserPreference(@PathVariable Long id) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /retrievePreference/{} invoked", id);
    }
    
    // Checks that a corresponding TarsUser with specified id already exists.
    TarsUser tarsUser = tarsUserService.findById(id);
    // If id was null, entry point would default to 404.
    if (tarsUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("TarsUser with id={} does not exist.", id);
      }
      return new ResponseEntity<>("TarsUser not found.", HttpStatus.NOT_FOUND);
    }

    for (UserPreference user : tarsService.getUserPreferenceList()) {
      if (user.getId().equals(id)) {
        if (logger.isInfoEnabled()) {
          logger.info("GET /retrievePreference/{} success", id);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
      }
    }

    if (logger.isWarnEnabled()) {
      logger.warn("GET /retrievePreference/{} not found", id);
    }
    return new ResponseEntity<>("User had no existing preferences.", HttpStatus.BAD_REQUEST);
  }

  // #TODO: /userPreferencesList
  /**
   * Handles GET requests to retrieve preference information about all the existing users.
   *
   * @return a ResponseEntity containing the User Preferences data in json format for all users
   *          if successful. Otherwise, return the status code INTERNAL_SERVER_ERROR. 
   */
  @GetMapping("/userPreferenceList")
  public ResponseEntity<List<UserPreference>> getUserList() {
    // #TODO: iterate through all existing TarsUser and get their preference or add user with 
    try {
      List<UserPreference> userPreferenceList = tarsService.getUserPreferenceList();
      return new ResponseEntity<>(userPreferenceList, HttpStatus.OK);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("GET /userList failed", e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve all TarsUsers from users.json.
   *
   * @return a ResponseEntity containing all TarsUser objects in json format if successful.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   */
  @GetMapping("/tarsUsers")
  public ResponseEntity<List<TarsUser>> getTarsUsers() {
    if (logger.isInfoEnabled()) {
      logger.info("GET /tarsUsers invoked");
    }
    try {
      List<TarsUser> tarsUsers = tarsUserService.listUsers();
      if (logger.isInfoEnabled()) {
        logger.info("GET /tarsUsers success: returned {} users", tarsUsers.size());
      }
      return new ResponseEntity<>(tarsUsers, HttpStatus.OK);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("GET /tarsUsers failed", e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles DELETE requests to delete a TarsUser by their userId.
   *
   * @param userId the unique identifier of the user to delete
   * @return a ResponseEntity containing the deleted TarsUser if successful,
   *         NOT_FOUND if user doesn't exist, or INTERNAL_SERVER_ERROR on failure.
   */
  @DeleteMapping("/tarsUsers/{userId}")
  public ResponseEntity<?> deleteTarsUser(@PathVariable long userId) {
    if (logger.isInfoEnabled()) {
      logger.info("DELETE /tarsUsers/{} invoked", userId);
    }
    try {
      TarsUser deletedUser = tarsUserService.deleteUser(userId);
      if (deletedUser == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("DELETE /tarsUsers/{} failed: user not found", userId);
        }
        return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
      }
      if (logger.isInfoEnabled()) {
        logger.info("DELETE /tarsUsers/{} success: deleted user '{}'", 
            userId, deletedUser.getUsername());
      }
      return new ResponseEntity<>(deletedUser, HttpStatus.OK);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("DELETE /tarsUsers/{} failed", userId, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  // /**
  //  * Handles GET requests to retrieve userPreference information about all existing users 
  //  * under a specified client.
  //  *
  //  * @param clientId the id of the client we want to retrieve user data for.
  //  * @return a ResponseEntity containing the User Preferences data in json format for all users
  //  *          under a client specified by clientId. Returns an empty json if no users are found.
  //  *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
  //  */
  // @GetMapping("/userList/client/{clientId}")
  // public ResponseEntity<List<UserPreference>> getClientUserList(@PathVariable int clientId) {
  //   try {
  //     List<UserPreference> userList = tarsService.getUserPreferenceList();
  //     List<UserPreference> clientUserList = new ArrayList<>();
  //     for (UserPreference user : userList) {
  //       if (user.getClientId() == clientId) {
  //         clientUserList.add(user);
  //       }
  //     }
  //     return new ResponseEntity<>(clientUserList, HttpStatus.OK);
  //   } catch (Exception e) {
  //     return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  //   }
  // }

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
      // if (logger.isDebugEnabled()) {
      //   logger.debug("Recommendation detail: {}", recommendation);
      // }
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
      // if (logger.isDebugEnabled()) {
      //   logger.debug("Alert detail: {}", alert);
      // }
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
  public ResponseEntity<?> getUserWeatherAlerts(@PathVariable Long userId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /alert/weather/user/{} invoked", userId);
    }
    try {
      UserPreference user = tarsService.getUserPreference(userId);
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
      String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
      return new ResponseEntity<>("Error: " + errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  
  /**
   * Retrieves the travel advisory for a given country.
   *
   * @param country the country to retrieve the advisory for
   * @return the travel advisory or an error response
   */
  @GetMapping("/country/{country}")
  public ResponseEntity<?> getCountryAdvisory(@PathVariable String country) {
    logger.info("GET /country/{} invoked", country);

    try {
      TravelAdvisoryModel model = new TravelAdvisoryModel();
      TravelAdvisory advisory = model.getTravelAdvisory(country);

      if (advisory == null) {
        logger.warn("No advisory found for country={}", country);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }

      return ResponseEntity.ok(advisory.toString());

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());

    } catch (Exception e) {
      logger.error("Error retrieving advisory for country={}", country, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Retrieves a city summary for a given city and optional date range.
   *
   * @param city the city to summarize
   * @param startDate the start date (optional)
   * @param endDate the end date (optional)
   * @return the city summary or an error response
   */
  @GetMapping("/summary/{city}")
  public ResponseEntity<?> getCitySummary(
      @PathVariable String city,
      @RequestParam(required = false) String state,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {


    if (logger.isInfoEnabled()) {
      logger.info("GET /summary/{} invoked with startDate={} endDate={}",
          city, startDate, endDate);
    }

    try { // @PathVariable can never be null, it return 404
      if (city == null || city.trim().isEmpty()) {
        if (logger.isWarnEnabled()) {
          logger.warn("City parameter is empty");
        }
        return new ResponseEntity<>("City cannot be empty.", HttpStatus.BAD_REQUEST);
      }

      // Parse dates if provided
      LocalDate start = null;
      LocalDate end = null;
      DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

      if (startDate != null && !startDate.trim().isEmpty()) {
        try {
          start = LocalDate.parse(startDate, formatter);
        } catch (DateTimeParseException e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Invalid startDate format: {}", startDate);
          }
          return new ResponseEntity<>(
              "Invalid startDate format. Expected YYYY-MM-DD.", HttpStatus.BAD_REQUEST);
        }
      }

      if (endDate != null && !endDate.trim().isEmpty()) {
        try {
          end = LocalDate.parse(endDate, formatter);
        } catch (DateTimeParseException e) {
          if (logger.isWarnEnabled()) {
            logger.warn("Invalid endDate format: {}", endDate);
          }
          return new ResponseEntity<>(
              "Invalid endDate format. Expected YYYY-MM-DD.", HttpStatus.BAD_REQUEST);
        }
      }

      // Validate date range
      if (start != null && end != null && start.isAfter(end)) {
        if (logger.isWarnEnabled()) {
          logger.warn("startDate {} is after endDate {}", startDate, endDate);
        }
        return new ResponseEntity<>(
            "startDate cannot be after endDate.", HttpStatus.BAD_REQUEST);
      }

      // Calculate number of days to fetch (default to 14 if no dates provided)
      int daysToFetch = 14;
      if (start != null && end != null) {
        daysToFetch = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        if (daysToFetch > 14) {
          daysToFetch = 14; // API limit
        }
        if (daysToFetch <= 0) {
          return new ResponseEntity<>(
              "Invalid date range.", HttpStatus.BAD_REQUEST);
        }
      }

      // Get weather recommendations
      WeatherRecommendation weatherRecommendation = 
          WeatherModel.getRecommendedDays(city, daysToFetch);

      // Get weather alerts for the city
      WeatherAlert weatherAlert = null;
      try {
        weatherAlert = WeatherAlertModel.getWeatherAlerts(city, null, null);
      } catch (Exception e) {
        if (logger.isWarnEnabled()) {
          logger.warn("Failed to fetch weather alerts for city={}: {}", city, e.getMessage());
        }
        // Continue without alerts if fetch fails
      }

      // Get travel advisory for the city's country
      TravelAdvisory travelAdvisory = null;
      boolean isUnitedStates = false;
      try {
        TravelAdvisoryModel advisoryModel = new TravelAdvisoryModel();
        String country = CitySummary.getCountryFromCity(city);
        if (country != null && !country.trim().isEmpty()) {
          travelAdvisory = advisoryModel.getTravelAdvisory(country);
        }
        if (country.equals("United States")) {
          isUnitedStates = true;
        }
        // If country lookup failed, try using city name as country name (works for some cases)
        if (travelAdvisory == null) {
          travelAdvisory = advisoryModel.getTravelAdvisory(city);
        }
      } catch (Exception e) {
        if (logger.isDebugEnabled()) {
          logger.debug("Failed to fetch travel advisory for city={}: {}", city, e.getMessage());
        }
        // Continue without travel advisory if fetch fails
      }

      // Filter recommended days by date range if provided
      if (start != null || end != null) {
        List<String> filteredDays = new ArrayList<>();
        if (weatherRecommendation.getRecommendedDays() != null) {
          for (String day : weatherRecommendation.getRecommendedDays()) {
            try {
              LocalDate dayDate = LocalDate.parse(day, formatter);
              boolean include = true;
              if (start != null && dayDate.isBefore(start)) {
                include = false;
              }
              if (end != null && dayDate.isAfter(end)) {
                include = false;
              }
              if (include) {
                filteredDays.add(day);
              }
            } catch (DateTimeParseException e) {
              // Skip days that don't match the expected format
              if (logger.isDebugEnabled()) {
                logger.debug("Skipping day with invalid format: {}", day);
              }
            }
          }
        }
        weatherRecommendation.setRecommendedDays(filteredDays);
        
        // Update message
        String filteredMessage;
        if (filteredDays.isEmpty()) {
          filteredMessage = String.format(
              "No clear days found in the date range %s to %s for %s.",
              startDate != null ? startDate : "start",
              endDate != null ? endDate : "end",
              city);
        } else {
          filteredMessage = String.format(
              "These days in the range %s to %s are expected to have clear weather in %s!",
              startDate != null ? startDate : "start",
              endDate != null ? endDate : "end",
              city);
        }
        weatherRecommendation.setMessage(filteredMessage);
      }

      // Get all users and filter those who have this city in their preferences
      List<UserPreference> allUsers = tarsService.getUserPreferenceList();
      List<UserPreference> interestedUsers = allUsers.stream()
          .filter(user -> user.getCityPreferences() != null
              && user.getCityPreferences().stream()
                  .anyMatch(pref -> pref.equalsIgnoreCase(city)))
          .collect(Collectors.toList());

      // Build message including alert and travel advisory information
      StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(String.format(
          "Summary for %s: Found %d user(s) interested in this city. %s",
          city, interestedUsers.size(), weatherRecommendation.getMessage()));
      
      if (weatherAlert != null && weatherAlert.getAlerts() != null 
          && !weatherAlert.getAlerts().isEmpty()) {
        // Check if there are any non-INFO alerts
        boolean hasActiveAlerts = weatherAlert.getAlerts().stream()
            .anyMatch(alert -> !"INFO".equals(alert.get("severity")) 
                && !"CLEAR".equals(alert.get("type")));
        
        if (hasActiveAlerts) {
          messageBuilder.append(" Active weather alerts present.");
        }
      }

      if (travelAdvisory != null) {
        if (travelAdvisory.getLevel() != null && !travelAdvisory.getLevel().isEmpty()) {
          messageBuilder.append(String.format(" Travel advisory: %s.", travelAdvisory.getLevel()));
        }
      }

      // If its a US city we can add crime data
      CrimeSummary crimeSummary = null;
      String offense = null;
      String month = null;
      String year = null;
      if (isUnitedStates && state != null) {
        try {
          CrimeModel model = new CrimeModel();

          if (state == null) {
            logger.warn("Could not determine state for US city {}", city);
          } else {

            LocalDate today = LocalDate.now();

            offense = "V";        // violent crime as default
            month = String.valueOf(today.getMonthValue());         //current month
            year = String.valueOf(today.getYear());;        // current year

            String result = model.getCrimeSummary(state, offense, month, year);

            if (logger.isDebugEnabled()) {
              logger.debug("Crime API result for state={} offense={} month={} year={}: {}",
                      state, offense, month, year, result);
            }

            crimeSummary = new CrimeSummary(
                    state,
                    month,
                    year,
                    "Fetched crime data for offense=" + offense + " : " + result
            );

            if (logger.isInfoEnabled()) {
              logger.info("Crime summary created for state={} offense={}", state, offense);
            }
          }

        } catch (Exception e) {
          if (logger.isErrorEnabled()) {
            logger.error("Error fetching crime summary for state={} offense={} month={} year={}",
                    state, offense, month, year, e);
          }
        }
      }

      CitySummary summary = new CitySummary(
              city,
              weatherRecommendation,
              weatherAlert,
              travelAdvisory,
              interestedUsers,
              crimeSummary,
              messageBuilder.toString()
      );

      if (logger.isInfoEnabled()) {
        logger.info("City summary generated for city={} with {} interested users",
            city, interestedUsers.size());
      }

      return ResponseEntity.ok(summary);

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error generating city summary for city={}", city, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

}
