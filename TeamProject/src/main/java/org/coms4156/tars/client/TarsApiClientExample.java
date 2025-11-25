package org.coms4156.tars.client;

import java.io.IOException;
import java.util.List;
import org.coms4156.tars.model.CitySummary;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherRecommendation;

/**
 * Example usage of the TarsApiClient.
 * This class demonstrates how to interact with all the TARS API endpoints.
 */
public class TarsApiClientExample {

  /**
   * Main method demonstrating client usage.
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    // Initialize the client (defaults to http://localhost:8080)
    TarsApiClient client = new TarsApiClient();

    // Or use a custom base URL
    // TarsApiClient client = new TarsApiClient("http://localhost:8080");

    try {
      // Example 1: Get welcome message
      System.out.println("=== Example 1: Get Index ===");
      String welcomeMessage = client.getIndex();
      System.out.println(welcomeMessage);
      System.out.println();

      // Example 2: Create a client
      System.out.println("=== Example 2: Create Client ===");
      var clientResponse = client.createClient("Test Client", "test@example.com");
      System.out.println("Client created: " + clientResponse);
      System.out.println();

      // Example 3: Create a user for a client
      System.out.println("=== Example 3: Create Client User ===");
      TarsUser newUser = client.createClientUser(1L, "john_doe", "john@example.com", "user");
      System.out.println("User created: " + newUser);
      System.out.println();

      // Example 4: Add user preferences
      System.out.println("=== Example 4: Add User Preferences ===");
      User user = new User(1, 1);
      user.setCityPreferences(List.of("New York", "Los Angeles"));
      user.setWeatherPreferences(List.of("Clear", "Sunny"));
      user.setTemperaturePreferences(List.of("70-80"));
      User addedUser = client.addUser(1, user);
      System.out.println("User preferences added: " + addedUser);
      System.out.println();

      // Example 5: Get user preferences
      System.out.println("=== Example 5: Get User ===");
      User retrievedUser = client.getUser(1);
      System.out.println("Retrieved user: " + retrievedUser);
      System.out.println();

      // Example 6: Get all users
      System.out.println("=== Example 6: Get User List ===");
      List<User> allUsers = client.getUserList();
      System.out.println("Total users: " + allUsers.size());
      allUsers.forEach(u -> System.out.println("  - User ID: " + u.getId()));
      System.out.println();

      // Example 7: Get weather recommendations
      System.out.println("=== Example 7: Get Weather Recommendations ===");
      WeatherRecommendation weatherRec = client.getWeatherRecommendation("New York", 7);
      System.out.println("Weather recommendation: " + weatherRec);
      System.out.println();

      // Example 8: Get weather alerts by city
      System.out.println("=== Example 8: Get Weather Alerts by City ===");
      WeatherAlert alertByCity = client.getWeatherAlertsByCity("New York");
      System.out.println("Weather alert: " + alertByCity);
      System.out.println();

      // Example 9: Get weather alerts by coordinates
      System.out.println("=== Example 9: Get Weather Alerts by Coordinates ===");
      WeatherAlert alertByCoords = client.getWeatherAlertsByCoordinates(40.7128, -74.0060);
      System.out.println("Weather alert: " + alertByCoords);
      System.out.println();

      // Example 10: Get user weather alerts
      System.out.println("=== Example 10: Get User Weather Alerts ===");
      List<WeatherAlert> userAlerts = client.getUserWeatherAlerts(1);
      System.out.println("User alerts count: " + userAlerts.size());
      userAlerts.forEach(a -> System.out.println("  - Alert: " + a.getLocation()));
      System.out.println();

      // Example 11: Get crime summary
      System.out.println("=== Example 11: Get Crime Summary ===");
      CrimeSummary crimeSummary = client.getCrimeSummary("North Carolina", "Robbery", "10", "2024");
      System.out.println("Crime summary: " + crimeSummary);
      System.out.println();

      // Example 12: Get country advisory
      System.out.println("=== Example 12: Get Country Advisory ===");
      String advisory = client.getCountryAdvisory("France");
      System.out.println("Travel advisory: " + advisory);
      System.out.println();

      // Example 13: Get city summary
      System.out.println("=== Example 13: Get City Summary ===");
      CitySummary citySummary = client.getCitySummary("New York");
      System.out.println("City summary: " + citySummary);
      System.out.println();

      // Example 14: Get city summary with date range
      System.out.println("=== Example 14: Get City Summary with Date Range ===");
      CitySummary citySummaryWithDates = client.getCitySummary(
          "New York", "2024-10-01", "2024-10-14");
      System.out.println("City summary with dates: " + citySummaryWithDates);
      System.out.println();

    } catch (IOException | InterruptedException e) {
      System.err.println("Error calling API: " + e.getMessage());
      e.printStackTrace();
    }
  }
}

