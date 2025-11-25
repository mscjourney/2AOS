package org.coms4156.tars.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.client.TarsApiClient;
import org.coms4156.tars.model.CitySummary;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherRecommendation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Web controller for the TARS UI.
 * Handles all web page requests and form submissions.
 */
@Controller
public class WebController {

  private static final Logger logger = LoggerFactory.getLogger(WebController.class);
  private final TarsApiClient apiClient;

  /**
   * Constructor for WebController.
   * Uses dependency injection to get TarsApiClient instance.
   * If no bean is provided, creates a default instance with localhost URL.
   *
   * @param apiClient the TarsApiClient instance (injected by Spring)
   */
  @Autowired
  public WebController(TarsApiClient apiClient) {
    this.apiClient = apiClient;
  }

  /**
   * Displays the main dashboard/home page.
   * Fetches welcome message and aggregates alerts from all users.
   *
   * @param model the model to add attributes to
   * @return the view name
   */
  @GetMapping("/ui")
  public String dashboard(Model model) {
    try {
      String welcomeMessage = apiClient.getIndex();
      model.addAttribute("welcomeMessage", welcomeMessage);
      
      // Fetch all users and get their alerts
      List<User> users = apiClient.getUserList();
      List<WeatherAlert> allAlerts = new java.util.ArrayList<>();
      
      for (User user : users) {
        try {
          List<WeatherAlert> userAlerts = apiClient.getUserWeatherAlerts(user.getId());
          if (userAlerts != null) {
            allAlerts.addAll(userAlerts);
          }
        } catch (Exception e) {
          logger.debug("Could not fetch alerts for user {}: {}", user.getId(), e.getMessage());
          // Continue with other users
        }
      }
      
      model.addAttribute("alerts", allAlerts);
      model.addAttribute("alertCount", allAlerts.size());
    } catch (Exception e) {
      logger.error("Error fetching dashboard data", e);
      model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
    }
    return "dashboard";
  }


  /**
   * Displays the create client form.
   *
   * @return the view name
   */
  @GetMapping("/ui/client/create")
  public String showCreateClientForm() {
    return "create-client";
  }

  /**
   * Handles client creation form submission.
   *
   * @param name the client name
   * @param email the client email
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/client/create")
  public String createClient(@RequestParam String name, @RequestParam String email,
      Model model) {
    try {
      Map<String, Object> result = apiClient.createClient(name, email);
      model.addAttribute("success", true);
      model.addAttribute("result", result);
    } catch (Exception e) {
      logger.error("Error creating client", e);
      model.addAttribute("error", "Failed to create client: " + e.getMessage());
    }
    return "create-client";
  }

  /**
   * Displays the create user form.
   *
   * @return the view name
   */
  @GetMapping("/ui/user/create")
  public String showCreateUserForm() {
    return "create-user";
  }

  /**
   * Handles user creation form submission.
   * Creates a TarsUser and automatically creates a User preference entry with preferences,
   * then redirects to the view users page.
   *
   * @param clientId the client ID
   * @param username the username
   * @param email the user email
   * @param role the user role
   * @param cities comma-separated city preferences (optional)
   * @param weather comma-separated weather preferences (optional)
   * @param temperature comma-separated temperature preferences (optional)
   * @param model the model to add attributes to
   * @return the view name (redirects to view users on success)
   */
  @PostMapping("/ui/user/create")
  public String createUser(@RequestParam Long clientId, @RequestParam String username,
      @RequestParam String email, @RequestParam String role,
      @RequestParam(required = false) String cities,
      @RequestParam(required = false) String weather,
      @RequestParam(required = false) String temperature, Model model) {
    try {
      // Create TarsUser (user account)
      TarsUser tarsUser = apiClient.createClientUser(clientId, username, email, role);
      
      // Create User preference entry with preferences so it appears in view users
      if (tarsUser.getUserId() != null) {
        User userPreferences = new User(tarsUser.getUserId().intValue(), clientId.intValue());
        
        // Set preferences if provided
        if (cities != null && !cities.trim().isEmpty()) {
          userPreferences.setCityPreferences(
              java.util.Arrays.asList(cities.split(",")));
        }
        if (weather != null && !weather.trim().isEmpty()) {
          userPreferences.setWeatherPreferences(
              java.util.Arrays.asList(weather.split(",")));
        }
        if (temperature != null && !temperature.trim().isEmpty()) {
          userPreferences.setTemperaturePreferences(
              java.util.Arrays.asList(temperature.split(",")));
        }
        
        try {
          apiClient.addUser(tarsUser.getUserId().intValue(), userPreferences);
        } catch (Exception e) {
          // If user already exists, that's okay - just log it
          logger.debug("User preference entry may already exist for userId: {}", 
              tarsUser.getUserId());
        }
      }
      
      // Redirect to view users page to see the newly created user
      return "redirect:/ui/users";
    } catch (Exception e) {
      logger.error("Error creating user", e);
      model.addAttribute("error", "Failed to create user: " + e.getMessage());
      return "create-user";
    }
  }

  /**
   * Handles user update form submission from the view users page.
   *
   * @param userId the user ID
   * @param clientId the client ID
   * @param cities comma-separated city preferences
   * @param weather comma-separated weather preferences
   * @param temperature comma-separated temperature preferences
   * @param redirectAttributes attributes to pass through redirect
   * @return redirect to view users page
   */
  @PostMapping("/ui/user/update")
  public String updateUserPreferences(@RequestParam int userId, @RequestParam int clientId,
      @RequestParam(required = false) String cities,
      @RequestParam(required = false) String weather,
      @RequestParam(required = false) String temperature, RedirectAttributes redirectAttributes) {
    try {
      User user = new User(userId, clientId);
      
      // Handle cities - set empty list if null or empty, otherwise split and trim
      if (cities != null && !cities.trim().isEmpty()) {
        java.util.List<String> cityList = new java.util.ArrayList<>();
        for (String city : cities.split(",")) {
          String trimmed = city.trim();
          if (!trimmed.isEmpty()) {
            cityList.add(trimmed);
          }
        }
        user.setCityPreferences(cityList);
      } else {
        user.setCityPreferences(new java.util.ArrayList<>());
      }
      
      // Handle weather - set empty list if null or empty, otherwise split and trim
      if (weather != null && !weather.trim().isEmpty()) {
        java.util.List<String> weatherList = new java.util.ArrayList<>();
        for (String w : weather.split(",")) {
          String trimmed = w.trim();
          if (!trimmed.isEmpty()) {
            weatherList.add(trimmed);
          }
        }
        user.setWeatherPreferences(weatherList);
      } else {
        user.setWeatherPreferences(new java.util.ArrayList<>());
      }
      
      // Handle temperature - set empty list if null or empty, otherwise split and trim
      if (temperature != null && !temperature.trim().isEmpty()) {
        java.util.List<String> tempList = new java.util.ArrayList<>();
        for (String temp : temperature.split(",")) {
          String trimmed = temp.trim();
          if (!trimmed.isEmpty()) {
            tempList.add(trimmed);
          }
        }
        user.setTemperaturePreferences(tempList);
      } else {
        user.setTemperaturePreferences(new java.util.ArrayList<>());
      }
      
      apiClient.updateUser(userId, user);
      // Add success message that will be shown after redirect
      redirectAttributes.addFlashAttribute("success", "User preferences updated successfully!");
    } catch (Exception e) {
      logger.error("Error updating user preferences", e);
      redirectAttributes.addFlashAttribute("error", "Failed to update preferences: " + e.getMessage());
    }
    // Redirect will cause a fresh GET request to /ui/users, which will reload the data
    return "redirect:/ui/users";
  }

  /**
   * Displays the view users page.
   *
   * @param model the model to add attributes to
   * @return the view name
   */
  @GetMapping("/ui/users")
  public String viewUsers(Model model) {
    try {
      List<User> users = apiClient.getUserList();
      model.addAttribute("users", users);
    } catch (Exception e) {
      logger.error("Error fetching users", e);
      model.addAttribute("error", "Failed to fetch users: " + e.getMessage());
    }
    return "view-users";
  }

  /**
   * Displays the weather recommendation form.
   *
   * @return the view name
   */
  @GetMapping("/ui/weather/recommendation")
  public String showWeatherRecommendationForm() {
    return "weather-recommendation";
  }

  /**
   * Handles weather recommendation request.
   * Also preserves any alert data if it exists.
   *
   * @param city the city name
   * @param days the number of days
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/weather/recommendation")
  public String getWeatherRecommendation(@RequestParam String city, @RequestParam int days,
      Model model) {
    try {
      WeatherRecommendation recommendation = apiClient.getWeatherRecommendation(city, days);
      model.addAttribute("recommendation", recommendation);
    } catch (Exception e) {
      logger.error("Error fetching weather recommendation", e);
      model.addAttribute("error", "Failed to get recommendation: " + e.getMessage());
    }
    return "weather-recommendation";
  }

  /**
   * Displays the weather alerts form.
   * Redirects to the combined weather page.
   *
   * @return redirect to weather recommendation page
   */
  @GetMapping("/ui/weather/alerts")
  public String showWeatherAlertsForm() {
    return "redirect:/ui/weather/recommendation";
  }

  /**
   * Handles weather alerts request.
   * Also preserves any recommendation data if it exists.
   *
   * @param city the city name (optional)
   * @param lat the latitude (optional)
   * @param lon the longitude (optional)
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/weather/alerts")
  public String getWeatherAlerts(@RequestParam(required = false) String city,
      @RequestParam(required = false) Double lat, @RequestParam(required = false) Double lon,
      Model model) {
    try {
      WeatherAlert alert;
      if (city != null && !city.trim().isEmpty()) {
        alert = apiClient.getWeatherAlertsByCity(city);
      } else if (lat != null && lon != null) {
        alert = apiClient.getWeatherAlertsByCoordinates(lat, lon);
      } else {
        model.addAttribute("error", "Please provide either city or coordinates");
        return "weather-recommendation";
      }
      model.addAttribute("alert", alert);
    } catch (Exception e) {
      logger.error("Error fetching weather alerts", e);
      model.addAttribute("error", "Failed to get alerts: " + e.getMessage());
    }
    return "weather-recommendation";
  }

  /**
   * Displays the user weather alerts form.
   *
   * @return the view name
   */
  @GetMapping("/ui/weather/user-alerts")
  public String showUserWeatherAlertsForm() {
    return "user-weather-alerts";
  }

  /**
   * Handles user weather alerts request.
   *
   * @param userId the user ID
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/weather/user-alerts")
  public String getUserWeatherAlerts(@RequestParam int userId, Model model) {
    try {
      List<WeatherAlert> alerts = apiClient.getUserWeatherAlerts(userId);
      model.addAttribute("alerts", alerts);
      model.addAttribute("userId", userId);
    } catch (Exception e) {
      logger.error("Error fetching user weather alerts", e);
      model.addAttribute("error", "Failed to get alerts: " + e.getMessage());
    }
    return "user-weather-alerts";
  }

  /**
   * Displays the crime summary form.
   *
   * @return the view name
   */
  @GetMapping("/ui/crime/summary")
  public String showCrimeSummaryForm() {
    return "crime-summary";
  }

  /**
   * Handles crime summary request.
   *
   * @param state the state name
   * @param offense the offense type
   * @param month the month
   * @param year the year
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/crime/summary")
  public String getCrimeSummary(@RequestParam String state, @RequestParam String offense,
      @RequestParam String month, @RequestParam String year, Model model) {
    try {
      CrimeSummary summary = apiClient.getCrimeSummary(state, offense, month, year);
      model.addAttribute("summary", summary);
    } catch (Exception e) {
      logger.error("Error fetching crime summary", e);
      model.addAttribute("error", "Failed to get crime summary: " + e.getMessage());
    }
    return "crime-summary";
  }

  /**
   * Displays the country advisory form.
   *
   * @return the view name
   */
  @GetMapping("/ui/country/advisory")
  public String showCountryAdvisoryForm() {
    return "country-advisory";
  }

  /**
   * Handles country advisory request.
   *
   * @param country the country name
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/country/advisory")
  public String getCountryAdvisory(@RequestParam String country, Model model) {
    try {
      String advisory = apiClient.getCountryAdvisory(country);
      model.addAttribute("advisory", advisory);
      model.addAttribute("country", country);
    } catch (Exception e) {
      logger.error("Error fetching country advisory", e);
      model.addAttribute("error", "Failed to get advisory: " + e.getMessage());
    }
    return "country-advisory";
  }

  /**
   * Displays the city summary form.
   *
   * @return the view name
   */
  @GetMapping("/ui/city/summary")
  public String showCitySummaryForm() {
    return "city-summary";
  }

  /**
   * Handles city summary request.
   * Uses the CitySummary model class to retrieve and display comprehensive city information
   * including weather recommendations, weather alerts, travel advisories, and interested users.
   *
   * @param city the city name
   * @param startDate the start date (optional)
   * @param endDate the end date (optional)
   * @param model the model to add attributes to
   * @return the view name
   */
  @PostMapping("/ui/city/summary")
  public String getCitySummary(@RequestParam String city,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate, Model model) {
    try {
      // Get CitySummary object from the API client
      // CitySummary contains: city, weatherRecommendation, weatherAlert, 
      // travelAdvisory, interestedUsers, and message
      CitySummary summary;
      if (startDate != null && !startDate.trim().isEmpty()
          || endDate != null && !endDate.trim().isEmpty()) {
        summary = apiClient.getCitySummary(city, startDate, endDate);
      } else {
        summary = apiClient.getCitySummary(city);
      }
      // Add CitySummary object to model for Thymeleaf template
      model.addAttribute("summary", summary);
    } catch (Exception e) {
      logger.error("Error fetching city summary", e);
      model.addAttribute("error", "Failed to get city summary: " + e.getMessage());
    }
    return "city-summary";
  }
}

