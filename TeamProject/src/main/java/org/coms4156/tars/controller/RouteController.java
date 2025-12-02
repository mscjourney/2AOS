package org.coms4156.tars.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.coms4156.tars.dto.ClientDto;
import org.coms4156.tars.dto.TarsUserDto;
import org.coms4156.tars.dto.UserPreferenceDto;
import org.coms4156.tars.exception.BadRequestException;
import org.coms4156.tars.exception.ConflictException;
import org.coms4156.tars.exception.ForbiddenException;
import org.coms4156.tars.exception.NotFoundException;
import org.coms4156.tars.mapper.DtoMapper;
import org.coms4156.tars.model.CitySummary;
import org.coms4156.tars.model.Client;
import org.coms4156.tars.model.CountryModel;
import org.coms4156.tars.model.CountrySummary;
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
   * Handles GET requests to retrieve all clients.
   *
   * @return a ResponseEntity containing a list of all Client objects if successful,
   *         or an error response if the operation fails
   */
  @GetMapping("/clients")
  public ResponseEntity<List<ClientDto>> getClients() {
    if (logger.isInfoEnabled()) {
      logger.info("GET /clients invoked");
    }
    try {
      List<Client> clients = clientService.getClientList();
      if (logger.isInfoEnabled()) {
        logger.info("GET /clients success: returned {} clients", clients.size());
      }
      return ResponseEntity.ok(DtoMapper.toClientDtos(clients));
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("GET /clients failed", e);
      }
      throw e; // handled by GlobalExceptionHandler
    }
  }

  /**
   * Handles GET requests to retrieve a single client by id.
   *
   * @param clientId the unique identifier of the client
   * @return 200 with Client if found; 404 if not found; 400 if id invalid; 500 on error
   */
  @GetMapping("/clients/{clientId}")
  public ResponseEntity<ClientDto> getClientById(@PathVariable long clientId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /clients/{} invoked", clientId);
    }
    if (clientId < 0) {
      throw new BadRequestException("Client Id cannot be negative.");
    }
    Client client = clientService.getClient(clientId);
    if (client == null) {
      throw new NotFoundException("Client not found.");
    }
    if (logger.isInfoEnabled()) {
      logger.info("GET /clients/{} success", clientId);
    }
    return ResponseEntity.ok(DtoMapper.toClientDto(client));
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
  public ResponseEntity<ClientDto> createClient(
      @RequestBody(required = false) Map<String, String> body) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/create invoked");
    }

    // Validate request body
    if (body == null || body.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: request body is null");
      }
      throw new BadRequestException("Missing 'name' and 'email'.");
    }
    if (logger.isDebugEnabled()) {
      logger.debug("POST /client/create raw body keys={}", body.keySet());
    }

    // Validate name
    if (!body.containsKey("name")) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: 'name' field missing");
      }
      throw new BadRequestException("Missing 'name'.");
    }
    String name = body.get("name") == null ? "" : body.get("name").trim();
    if (name.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: blank name provided");
      }
      throw new BadRequestException("Client name cannot be blank.");
    }

    // Validate email
    if (!body.containsKey("email")) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: 'email' field missing");
      }
      throw new BadRequestException("Missing 'email'.");
    }
    String email = body.get("email") == null ? "" : body.get("email").trim();
    if (email.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: blank email provided");
      }
      throw new BadRequestException("Client email cannot be blank.");
    }
    if (!isValidEmail(email)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create failed: invalid email format '{}'", email);
      }
      throw new BadRequestException("Invalid email format.");
    }

    // Check name uniqueness
    if (!clientService.uniqueNameCheck(name)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create conflict: name '{}' already exists", name);
      }
      throw new ConflictException("Client name already exists.");
    }

    // Check email uniqueness
    if (!clientService.uniqueEmailCheck(email)) {
      if (logger.isWarnEnabled()) {
        logger.warn("POST /client/create conflict: email '{}' already exists", email);
      }
      throw new ConflictException("Client email already exists.");
    }
    
    Client created = clientService.createClient(name, email);
    if (created == null) {
      if (logger.isErrorEnabled()) {
        logger.error("POST /client/create internal error: client creation returned null name='{}'",
            name);
      }
      throw new RuntimeException("Failed to create client.");
    }
    
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/create success: clientId={} (view key in admin portal)", 
          created.getClientId());
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toClientDto(created));
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
  public ResponseEntity<TarsUserDto> createClientUser(
      @RequestBody(required = false) TarsUser newUserRequestBody) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/createUser invoked");
    }
    if (newUserRequestBody == null) {
      throw new BadRequestException("Body cannot be null.");
    }

    Long clientId = newUserRequestBody.getClientId();
    if (clientId == null || clientId < 0) {
      throw new BadRequestException("Invalid clientId.");
    }

    String username = newUserRequestBody.getUsername() == null
        ? ""
        : newUserRequestBody.getUsername().trim();
    if (username.isEmpty()) {
      throw new BadRequestException("Username cannot be blank.");
    }

    String email = newUserRequestBody.getEmail() == null
        ? ""
        : newUserRequestBody.getEmail().trim();
    if (email.isEmpty()) {
      throw new BadRequestException("Email cannot be blank.");
    }
    if (!isValidEmail(email)) {
      throw new BadRequestException("Invalid email format.");
    }

    String role = newUserRequestBody.getRole() == null ? "" : newUserRequestBody.getRole().trim();
    if (role.isEmpty()) {
      throw new BadRequestException("Role cannot be blank.");
    }

    if (logger.isDebugEnabled()) {
      logger.debug(
          "POST /client/createUser validation passed clientId={} username='{}' role='{}'",
          clientId, username, role);
    }

    Client client = clientService.getClient(clientId.intValue());
    if (client == null) {
      throw new NotFoundException("Client not found.");
    }

    if (tarsUserService.existsByClientIdAndUsername(clientId, username)) {
      throw new ConflictException("Username already exists for this client.");
    }
    if (tarsUserService.existsByClientIdAndUserEmail(clientId, email)) {
      throw new ConflictException("A user with the email already exists for this client.");
    }

    TarsUser created = tarsUserService.createUser(clientId, username, email, role);
    if (created == null) {
      throw new RuntimeException("Failed to create user.");
    }
    if (logger.isDebugEnabled()) {
      logger.debug(
          "POST /client/createUser created user detail userId={} clientId={} "
          + "username='{}' email='{}' role='{}'",
          created.getUserId(), created.getClientId(), created.getUsername(),
          created.getEmail(), created.getRole());
    }
    if (logger.isInfoEnabled()) {
      logger.info("POST /client/createUser success newUserId={}", created.getUserId());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toTarsUserDto(created));
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
  public ResponseEntity<List<UserPreferenceDto>> getUserList() {
    // #TODO: iterate through all existing TarsUser and get their preference or add user with 
    List<UserPreferenceDto> dto =
        DtoMapper.toUserPreferenceDtos(tarsService.getUserPreferenceList());
    return new ResponseEntity<>(dto, HttpStatus.OK);
  }

  /**
   * Handles GET requests to retrieve all TarsUsers from users.json.
   *
   * @return a ResponseEntity containing all TarsUser objects in json format if successful.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   */
  @GetMapping("/tarsUsers")
  public ResponseEntity<List<TarsUserDto>> getTarsUsers() {
    if (logger.isInfoEnabled()) {
      logger.info("GET /tarsUsers invoked");
    }
    try {
      List<TarsUser> tarsUsers = tarsUserService.listUsers();
      if (logger.isInfoEnabled()) {
        logger.info("GET /tarsUsers success: returned {} users", tarsUsers.size());
      }
      return new ResponseEntity<>(DtoMapper.toTarsUserDtos(tarsUsers), HttpStatus.OK);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("GET /tarsUsers failed", e);
      }
      throw e;
    }
  }

  /**
   * Handles GET requests to retrieve a single TarsUser by id.
   *
   * @param userId the unique identifier of the user
   * @return 200 with TarsUser if found; 404 if not found; 400 if id invalid; 500 on error
   */
  @GetMapping("/tarsUsers/{userId}")
  public ResponseEntity<TarsUserDto> getTarsUserById(@PathVariable long userId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /tarsUsers/{} invoked", userId);
    }
    if (userId < 0) {
      throw new BadRequestException("User Id cannot be negative.");
    }
    TarsUser user = tarsUserService.findById(userId);
    if (user == null) {
      throw new NotFoundException("TarsUser not found.");
    }
    if (logger.isInfoEnabled()) {
      logger.info("GET /tarsUsers/{} success", userId);
    }
    return ResponseEntity.ok(DtoMapper.toTarsUserDto(user));
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

  /**
   * Handles GET requests to retrieve user preferences for a specific clientId.
   * This finds the TarsUser with the matching clientId and returns their preferences.
   *
   * @param clientId the id of the client we want to retrieve user data for
   * @return a ResponseEntity containing the UserPreference for the user
   *         with the specified clientId, or empty preferences if not found
   */
  @GetMapping("/user/client/{clientId}")
  public ResponseEntity<UserPreferenceDto> getUserByClientId(@PathVariable Long clientId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /user/client/{} invoked", clientId);
    }
    if (clientId == null || clientId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("GET /user/client/{} failed: negative or null clientId", clientId);
      }
      throw new BadRequestException("Client Id cannot be negative.");
    }

    List<TarsUser> clientUsers = tarsUserService.listUsersByClientId(clientId);
    TarsUser foundUser = clientUsers.isEmpty() ? null : clientUsers.get(0);
    if (foundUser == null) {
      if (logger.isWarnEnabled()) {
        logger.warn("GET /user/client/{} failed: no user found for clientId", clientId);
      }
      throw new NotFoundException("No user found for clientId.");
    }

    UserPreference userPrefs = tarsService.getUserPreference(foundUser.getUserId());
    if (userPrefs == null) { // create empty preference for user without prefs
      userPrefs = new UserPreference(foundUser.getUserId());
      if (logger.isInfoEnabled()) {
        logger.info("GET /user/client/{} success: user found but no preferences", clientId);
      }
    } else if (logger.isInfoEnabled()) {
      logger.info("GET /user/client/{} success: found user preferences", clientId);
    }
    return ResponseEntity.ok(DtoMapper.toUserPreferenceDto(userPrefs));
  }

  /**
   * Handles GET requests to retrieve all user preferences
   *          under a client specified by clientId. Returns an empty json if no users are found.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   *
   * @param clientId the id of the client we want to retrieve user data for
   * @return a ResponseEntity containing the User Preferences data in json format for all users
   *          under a client specified by clientId. Returns an empty json if no users are found.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   */
  @GetMapping("/userList/client/{clientId}")
  public ResponseEntity<List<UserPreferenceDto>> getClientUserList(@PathVariable Long clientId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /userList/client/{} invoked", clientId);
    }
    if (clientId == null || clientId < 0) {
      if (logger.isWarnEnabled()) {
        logger.warn("GET /userList/client/{} failed: negative or null clientId", clientId);
      }
      throw new BadRequestException("Client Id cannot be negative.");
    }

    List<TarsUser> clientUsers = tarsUserService.listUsersByClientId(clientId);
    List<UserPreference> clientUserList = new ArrayList<>();
    for (TarsUser user : clientUsers) {
      UserPreference prefs = tarsService.getUserPreference(user.getUserId());
      clientUserList.add(prefs != null ? prefs : new UserPreference(user.getUserId()));
    }
    if (logger.isInfoEnabled()) {
      logger.info("GET /userList/client/{} success: found {} users",
          clientId, clientUserList.size());
    }
    return ResponseEntity.ok(DtoMapper.toUserPreferenceDtos(clientUserList));
  }

  /**
   * Handles POST requests for user login.
   * Finds a user by username, email, or userId and returns their data with preferences.
   *
   * @param body JSON object containing username, email, or userId
   * @return a ResponseEntity containing user data and preferences if successful,
   *         or an error message if user not found or inactive
   */
  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
    if (logger.isInfoEnabled()) {
      logger.info("POST /login invoked");
    }
    String username = body != null ? body.get("username") : null;
    String email = body != null ? body.get("email") : null;
    String userIdStr = body != null ? body.get("userId") : null;

    if (username == null && email == null && userIdStr == null) {
      throw new BadRequestException("Username, email, or userId is required");
    }

    TarsUser foundUser = null;
    List<TarsUser> allUsers = tarsUserService.listUsers();
    if (userIdStr != null) {
      try {
        Long userId = Long.parseLong(userIdStr);
        foundUser = tarsUserService.findById(userId);
      } catch (NumberFormatException e) {
        throw new BadRequestException("Invalid userId format.");
      }
    } else if (username != null) {
      String searchUsername = username.toLowerCase().trim();
      for (TarsUser user : allUsers) {
        if (user.getUsername() != null && user.getUsername().toLowerCase().equals(searchUsername)) {
          foundUser = user;
          break;
        }
      }
    } else if (email != null) {
      String searchEmail = email.toLowerCase().trim();
      for (TarsUser user : allUsers) {
        if (user.getEmail() != null && user.getEmail().toLowerCase().equals(searchEmail)) {
          foundUser = user;
          break;
        }
      }
    }

    if (foundUser == null) {
      throw new NotFoundException("User not found. Please check your credentials.");
    }
    if (!foundUser.getActive()) {
      throw new ForbiddenException("User account is inactive.");
    }

    UserPreference userPrefs = tarsService.getUserPreference(foundUser.getUserId());
    if (userPrefs == null) {
      userPrefs = new UserPreference(foundUser.getUserId());
    }

    Map<String, Object> response = new java.util.HashMap<>();
    response.put("userId", foundUser.getUserId());
    response.put("clientId", foundUser.getClientId());
    response.put("username", foundUser.getUsername());
    response.put("email", foundUser.getEmail());
    response.put("role", foundUser.getRole());

    Map<String, Object> preferences = new java.util.HashMap<>();
    preferences.put("cityPreferences", userPrefs.getCityPreferences());
    preferences.put("weatherPreferences", userPrefs.getWeatherPreferences());
    preferences.put("temperaturePreferences", userPrefs.getTemperaturePreferences());
    response.put("preferences", preferences);

    if (logger.isInfoEnabled()) {
      logger.info("Login successful for user userId={}", foundUser.getUserId());
    }
    return ResponseEntity.ok(response);
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
   * Retrieves a country summary for a given country.
   *
   * @param country the country to summarize
   * @return the country summary or an error response
   */
  @GetMapping("/countrySummary/{country}")
  public ResponseEntity<?> getCountrySummary(@PathVariable String country) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /countrySummary/{} invoked", country);
    }

    try {
      CountryModel countryModel = new CountryModel();
      CountrySummary summary = countryModel.getCountrySummary(country);

      if (summary == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("No country summary found for country={}", country);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body("Country not found: " + country);
      }

      // Get travel advisory for the country
      TravelAdvisoryModel advisoryModel = new TravelAdvisoryModel();
      TravelAdvisory advisory = advisoryModel.getTravelAdvisory(country);

      // Set the travel advisory in the summary
      summary.setTravelAdvisory(advisory);

      if (logger.isInfoEnabled()) {
        logger.info("GET /countrySummary/{} success", country);
      }
      return ResponseEntity.ok(summary);

    } catch (IllegalArgumentException e) {
      if (logger.isWarnEnabled()) {
        logger.warn("GET /countrySummary/{} failed: {}", country, e.getMessage());
      }
      return ResponseEntity.badRequest().body(e.getMessage());

    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error retrieving country summary for country={}", country, e);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error retrieving country summary: " + e.getMessage());
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
        if ("United States".equals(country)) {
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
          LocalDate today = LocalDate.now();

          offense = "V";        // violent crime as default
          month = String.valueOf(today.getMonthValue());         // current month
          year = String.valueOf(today.getYear()); // current year

          final CrimeModel crimeModel = new CrimeModel();
          String result = crimeModel.getCrimeSummary(state, offense, month, year);

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
