package org.coms4156.tars;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    for (User user : tarsService.getUsers()) {
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
  @GetMapping("/recommendation/weather")
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
}