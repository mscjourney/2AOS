package org.coms4156.tars.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.dto.ClientDto;
import org.coms4156.tars.dto.TarsUserDto;
import org.coms4156.tars.dto.UserPreferenceDto;
import org.coms4156.tars.exception.BadRequestException;
import org.coms4156.tars.exception.ConflictException;
import org.coms4156.tars.exception.ForbiddenException;
import org.coms4156.tars.exception.NotFoundException;
import org.coms4156.tars.mapper.DtoMapper;
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

  /**
   * Handles GET requests to retrieve preference information about all the existing users.
   *
   * @return a ResponseEntity containing the User Preferences data in json format for all users
   *          if successful. Otherwise, return the status code INTERNAL_SERVER_ERROR. 
   */
  @GetMapping("/userPreferenceList")
  public ResponseEntity<List<UserPreferenceDto>> getUserList() {
    if (logger.isInfoEnabled()) {
      logger.info("GET /userPreferenceList invoked");
    }
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
      // Remove their preferences if there is any.
      tarsService.clearPreference(userId);

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
   * Handles GET requests to retrieve all user preferences
   *          under a client specified by clientId. Returns an empty json if no users are found.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   *
   * @param clientId the id of the client we want to retrieve user data for
   * @return a ResponseEntity containing the User Preferences data in json format for all users
   *          under a client specified by clientId. Returns an empty json if no users are found.
   *          Otherwise, return the status code INTERNAL_SERVER_ERROR.
   */
  @GetMapping("/userPreferenceList/client/{clientId}")
  public ResponseEntity<?> getClientUserList(@PathVariable Long clientId) {
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
        Long userId = Long.valueOf(userIdStr);
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
   *
   * @param city the city we want to generate the weather recommendation for
   * @param days the day range to consider when making the recommendation
   * @return a ResponseEntity containing the recommended days for the city if successful,
   *      BAD_REQUEST on invalid params, or INTERNAL FAILURE on API Failure.
   */
  @GetMapping("/recommendation/weather/")
  public ResponseEntity<?> getWeatherRecommendation(
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
        return new ResponseEntity<>("Days must be in the range [0, 14], inclusively.", 
                                      HttpStatus.BAD_REQUEST);
      }

      WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(city, days);
      if (logger.isInfoEnabled()) {
        logger.info("Weather recommendation generated for city={} days={}", city, days);
      }

      return ResponseEntity.ok(recommendation);
    } catch (IllegalArgumentException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid City = {} was passed in", city, e);
      }
      return new ResponseEntity<>("City could not be found.", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error generating recommendation city={} days={}", city, days, e);
      }
      return new ResponseEntity<>("API Failure.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather recommendations for a specified city
   * and number of forecast days given user preferences.
   *
   * @param userId the id of the user we are creating weather recommendations for.
   * @param city the city we want to generate the weather recommendation for
   * @param days the day range to consider when making the recommendation
   * @return a ResponseEntity containing the recommended days for the City using user's 
   *      weather preferences if successful, NOT_FOUND if TarsUser or their preferences do not 
   *      exist, or BAD_REQUEST on invalid params, or INTERNAL FAILURE on API Failure.
   */
  @GetMapping("/recommendation/weather/user/{userId}")
  public ResponseEntity<?> getUserWeatherRecommendation(@PathVariable Long userId,
          @RequestParam String city,
          @RequestParam int days) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /recommendation/weather city={} days={}", city, days);
    }
    try {
      if (userId < 0) {
        if (logger.isWarnEnabled()) {
          logger.warn("Negative userId provided: {}", userId);
        }
        return new ResponseEntity<>("User Id cannot be negative.", HttpStatus.BAD_REQUEST);
      }

      if (days <= 0 || days > 14) {
        if (logger.isWarnEnabled()) {
          logger.warn("Invalid days parameter: {}", days);
        }
        return new ResponseEntity<>("Days must be in the range [0, 14], inclusively.", 
                                      HttpStatus.BAD_REQUEST);
      }

      TarsUser tarsUser = tarsUserService.findById(userId);
      if (tarsUser == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("TarsUser with id={} does not exist.", userId);
        }
        return new ResponseEntity<>("TarsUser not found.", HttpStatus.NOT_FOUND);
      }

      UserPreference userPreference = tarsService.getUserPreference(userId);
      if (userPreference == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("No user found for id={}", userId);
        }
        return new ResponseEntity<>("User Preferences could not be found.", HttpStatus.NOT_FOUND);
      }

      WeatherRecommendation recommendation 
          = WeatherModel.getUserRecDays(city, days, userPreference);

      if (logger.isInfoEnabled()) {
        logger.info("Weather recommendation generated for city={} days={}", city, days);
      }

      return ResponseEntity.ok(recommendation);

    } catch (IllegalArgumentException e) {
      if (logger.isErrorEnabled()) {
        logger.error("Invalid City = {} was passed in", city, e);
      }
      return new ResponseEntity<>("City could not be found.", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      if (logger.isErrorEnabled()) {
        logger.error("Error generating recommendation city={} days={}", city, days, e);
      }
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather alerts for a specified location.
   *
   * @param city the city we want to retrieve weather alerts for
   *    Can be null if lat & lon provided
   * @param lat the latitude of the location we want to retrieve weather alerts for
   *    Can be null if city provided
   * @param lon the longitude of the location we want to retrieve weather alerts for
   *    Can be null if city provided
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
   *
   * @param userId the userId of a TarsUser who will be generating weather alerts for.
   * @return the list of weather alerts containing a weather alert for each of the city in
   *    the specfied user's city preferences if sucessful. BAD_REQUEST if userId is negative.
   *    NOT_FOUND if TarsUser associated with UserId doesn't exist or preferences of the TarsUser
   *    cannot be found. INTERNAL_SERVER_ERROR upon API Failure.
   */
  @GetMapping("/alert/weather/user/{userId}")
  public ResponseEntity<?> getUserWeatherAlerts(@PathVariable Long userId) {
    if (logger.isInfoEnabled()) {
      logger.info("GET /alert/weather/user/{} invoked", userId);
    }
    try {
      if (userId < 0) {
        if (logger.isWarnEnabled()) {
          logger.warn("Negative userId provided: {}", userId);
        }
        return new ResponseEntity<>("User Id cannot be less than zero.", HttpStatus.BAD_REQUEST);
      }

      TarsUser tarsUser = tarsUserService.findById(userId);
      // If id was null, entry point would default to 404.
      if (tarsUser == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("TarsUser with id={} does not exist.", userId);
        }
        return new ResponseEntity<>("TarsUser not found.", HttpStatus.NOT_FOUND);
      }

      UserPreference userPreference = tarsService.getUserPreference(userId);
      if (userPreference == null) {
        if (logger.isWarnEnabled()) {
          logger.warn("UserPreference for id={} has not been set.", userId);
        }
        return new ResponseEntity<>("User Preferences not found.", HttpStatus.NOT_FOUND);
      }

      List<WeatherAlert> alertList = WeatherAlertModel.getUserAlerts(userPreference);
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
   * 
   * @param state the state we are retrieving crime summary data for.
   * @param offense the type of offense in particular that we are retrieving
   * @param month the month in which we want the crime data for.
   * @param year, the year in which we want the crime data for.
   * @return a summary of the crime data in the specified state for the specified offense
   *      during month/year. BAD_REQUEST if API rejected with BAD_REQUEST.
   *      INTERNAL_SERVER_ERROR on API Failure.
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
      // getCrimeSummary only returns null if API returned BAD_REQUEST
      if (result == null) {
        return new ResponseEntity<>("Error: Invalid inputs have been passed in.", 
              HttpStatus.BAD_REQUEST);
      }

      CrimeSummary summary = new CrimeSummary(
          state,
          month,
          year,
          "Crime Data for " + offense + " : " + result
      );

      if (logger.isInfoEnabled()) {
        logger.info("Crime summary constructed state={} offense={}", state, offense);
      }
      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
        return new ResponseEntity<>("No such country found.", HttpStatus.NOT_FOUND);
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

}