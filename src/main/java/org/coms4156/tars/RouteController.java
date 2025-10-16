package org.coms4156.tars;

import Model.WeatherRecommendation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RouteController {
  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }

  @GetMapping("/recommendation/weather")
  public ResponseEntity<WeatherRecommendation> getWeatherRecommendation(
          @RequestParam String city) {

    // Create dummy weather recommendation
    WeatherRecommendation recommendation = new WeatherRecommendation(
            city,
            List.of("Tuesday", "Thursday", "Saturday"),
            "These days are expected to have nice weather in " + city + "!"
    );

    return ResponseEntity.ok(recommendation);
  }

}