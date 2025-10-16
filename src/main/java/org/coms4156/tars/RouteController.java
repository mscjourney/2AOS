package org.coms4156.tars;

import model.WeatherModel;
import model.WeatherRecommendation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class defines an API that access weather endpoints.
 */
@RestController
public class RouteController {
  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }

  /**
   * Handles GET requests to retrieve weather recommendations for a specified city
   * and number of forecast days.
   *
   * @param city the name of the city for which to retrieve weather recommendations
   * @param days the number of days to include in the forecast (must be between 1 and 14)
   * @return a ResponseEntity containing a model.WeatherRecommendation
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

}