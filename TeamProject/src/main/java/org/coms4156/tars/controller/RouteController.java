package org.coms4156.tars.controller;

import java.util.List;
import org.coms4156.tars.model.CrimeModel;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherAlertModel;
import org.coms4156.tars.model.WeatherModel;
import org.coms4156.tars.model.WeatherRecommendation;
import org.coms4156.tars.service.TarsService;
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

  private final TarsService tarsService;

  public RouteController(TarsService tarsService) {
    this.tarsService = tarsService;
  }

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }


  /**
   * An endpoint to create a new client.
   * Request Method: POST
   * Returns a new client resource.
   *
   * @param clientId The ID of the client.
   *
   * @return A ResponseEntity indicating the result of the operation.
   */
  @PostMapping({"/clients/{clientId}"})
  public ResponseEntity<String> createClientRoute(@PathVariable String clientId) {
    // Logic to create a client would go here.
    return new ResponseEntity<>(
        "Client route created for clientId: " + clientId, HttpStatus.CREATED
      );
  }

  
  /**
   * Handles POST requests to create a user for a specific client.
   *
   * @param clientId The ID of the client.
   * @param userId The ID of the user.
   *
   * @return A ResponseEntity indicating the result of the operation.
  */
  @PostMapping({"/clients/{clientId}/users/{userId}"})
  public ResponseEntity<String> createClientUserRoute(
      @PathVariable String clientId,
      @PathVariable String userId) {
    // Logic to create a user would go here.
    return new ResponseEntity<>(
        "User route created for userId: " + userId, HttpStatus.CREATED
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
    if (tarsService.addUser(user)) {
      return new ResponseEntity<>(user, HttpStatus.OK);
    }

    return new ResponseEntity<>("User Id already exists.", HttpStatus.CONFLICT);
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
    for (User user : tarsService.getUserList()) {
      if (user.getId() == id) {
        return new ResponseEntity<>(user, HttpStatus.OK);
      }
    }
    return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
  }

  /**
   * Handles GET requests to retrieve weather recommendations for a specified city
   * and number of forecast days.
   *
   * @param city the name of the city for which to retrieve weather recommendations
   * @param days the number of days to include in the forecast (must be between 1 and 14)
   * @return a ResponseEntity containing a WeatherRecommendation
   *         if successful, or an error status if validation fails or an exception occurs
   */
  @GetMapping("/recommendation/weather/")
  public ResponseEntity<WeatherRecommendation> getWeatherRecommendation(
          @RequestParam String city,
          @RequestParam int days) {
    try {
      if (days <= 0 || days > 14) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      WeatherRecommendation recommendation = WeatherModel.getRecommendedDays(city, days);

      return ResponseEntity.ok(recommendation);

    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather alerts for a specified location.
   *
   * @param city the name of the city (optional if lat/lon provided)
   * @param lat the latitude coordinate (optional if city provided)
   * @param lon the longitude coordinate (optional if city provided)
   * @return a ResponseEntity containing a WeatherAlert if successful,
   *         or an error status if validation fails or an exception occurs
   */
  @GetMapping("/alert/weather")
  public ResponseEntity<WeatherAlert> getWeatherAlerts(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) Double lat,
      @RequestParam(required = false) Double lon) {

    try {
      if (city == null && (lat == null || lon == null)) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }

      WeatherAlert alert = WeatherAlertModel.getWeatherAlerts(city, lat, lon);

      return ResponseEntity.ok(alert);

    } catch (IllegalArgumentException e) {
      System.err.println(e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve weather alerts for the specified user 
   * based on their city preferences.
   *
   * @param userId the user id of the user we are getting weather alerts for
   * @return a ResponseEntity containing a list of WeatherAlerts if successful,
   *         or an error status if an exception occurs
   */
  @GetMapping("/alert/weather/user/{userId}")
  public ResponseEntity<?> getUserWeatherAlerts(@PathVariable int userId) {
    try {
      User user = tarsService.getUser(userId);
      if (userId < 0) {
        return new ResponseEntity<>("User Id cannot be less than zero.", HttpStatus.BAD_REQUEST);
      }

      if (user == null) {
        return new ResponseEntity<>("No such user.", HttpStatus.NOT_FOUND);
      }
      
      List<WeatherAlert> alertList = WeatherAlertModel.getUserAlerts(user);
      return ResponseEntity.ok(alertList);
      
    } catch (IllegalArgumentException e) {
      System.err.println(e);
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      System.err.println(e);
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Handles GET requests to retrieve crime summary data for a specified state and offense type.
   * Offense types:
   *   V – Violent Crime (aggregate: ASS, RPE, ROB, HOM)
   *   ASS – Aggravated Assault
   *   BUR – Burglary
   *   LAR – Larceny-Theft
   *   MVT – Motor Vehicle Theft
   *   HOM – Homicide (Murder and Nonnegligent Manslaughter)
   *   RPE – Rape
   *   ROB – Robbery
   *   ARS – Arson
   *   P – Property Crime (aggregate: BUR, LAR, MVT, ARS)
   *
   * @param state   the U.S. state abbreviation (e.g., "CA", "NY", "TX")
   * @param offense the offense code (e.g., "ASS", "BUR", "HOM", "ROB")
   * @param month the month of interest (e.g., "10" for October)
   * @param year the year of interest (e.g., "2025")
   * @return a ResponseEntity containing a CrimeSummary or an error message
   */
  @GetMapping("/crime/summary")
  public ResponseEntity<?> getCrimeSummary(
          @RequestParam String state,
          @RequestParam String offense,
          @RequestParam String month,
          @RequestParam String year) {

    try {
      CrimeModel model = new CrimeModel();
      String result = model.getCrimeSummary(state, offense, month, year);

      CrimeSummary summary = new CrimeSummary(
              state,
              month,
              year,
              "Fetched crime data successfully for " + offense + " : " + result
      );

      return ResponseEntity.ok(summary);

    } catch (Exception e) {
      e.printStackTrace();
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}
