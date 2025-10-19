package org.coms4156.tars;

import model.WeatherAlert;
import model.WeatherAlertModel;
import model.WeatherModel;
import model.WeatherRecommendation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< HEAD
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class defines an API that accesses weather endpoints.
 */
@RestController
public class RouteController {
  
  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }


  /**
   * Create new client route
   * @param clientId
   *
   * @return
   */
  @PostMapping("/api/v1/clients/{clientId}")
  public ResponseEntity<String> createClientRoute(@PathVariable String clientId) {
    // Logic to create a client would go here.
    return new ResponseEntity<>("Client route created for clientId: " + clientId, HttpStatus.CREATED);
  }
  
  /**
   * Handles POST requests to create a user.
   * @param clientId The ID of the client.
   * @param userId The ID of the user.
   *
   * @return A ResponseEntity indicating the result of the operation.
  */
  @PostMapping("/api/v1/clients/{clientId}/users/{userId}")
  public ResponseEntity<String> createClientUserRoute(@PathVariable String userId) {
    // Logic to create a user would go here.
    return new ResponseEntity<>("User route created for userId: " + userId, HttpStatus.CREATED);
  
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
