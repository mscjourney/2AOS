package org.coms4156.tars.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.coms4156.tars.model.CitySummary;
import org.coms4156.tars.model.CrimeSummary;
import org.coms4156.tars.model.TarsUser;
import org.coms4156.tars.model.User;
import org.coms4156.tars.model.WeatherAlert;
import org.coms4156.tars.model.WeatherRecommendation;
import org.springframework.stereotype.Component;

/**
 * Client for interacting with the TARS API.
 * Provides methods to call all available endpoints.
 */
@Component
public class TarsApiClient {
  private final String baseUrl;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  /**
   * Creates a new TarsApiClient with the default base URL (http://localhost:8080).
   */
  public TarsApiClient() {
    this("http://localhost:8080");
  }

  /**
   * Creates a new TarsApiClient with a custom base URL.
   *
   * @param baseUrl the base URL of the TARS API (e.g., "http://localhost:8080")
   */
  public TarsApiClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Gets the welcome message from the index endpoint.
   *
   * @return the welcome message string
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public String getIndex() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/"))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

  /**
   * Creates a new client.
   *
   * @param name the client name
   * @param email the client email
   * @return a map containing the response data
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public Map<String, Object> createClient(String name, String email)
      throws IOException, InterruptedException {
    Map<String, String> body = Map.of("name", name, "email", email);
    String jsonBody = objectMapper.writeValueAsString(body);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/client/create"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
  }

  /**
   * Creates a new user for a client.
   *
   * @param clientId the client ID
   * @param username the username
   * @param email the user email
   * @param role the user role
   * @return the created TarsUser
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public TarsUser createClientUser(Long clientId, String username, String email, String role)
      throws IOException, InterruptedException {
    TarsUser user = new TarsUser(clientId, username, email, role);
    String jsonBody = objectMapper.writeValueAsString(user);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/client/createUser"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), TarsUser.class);
  }

  /**
   * Adds user preferences for a user.
   *
   * @param userId the user ID
   * @param user the User object with preferences
   * @return the User object with preferences
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public User addUser(int userId, User user) throws IOException, InterruptedException {
    String jsonBody = objectMapper.writeValueAsString(user);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/user/" + userId + "/add"))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), User.class);
  }

  /**
   * Updates user preferences for an existing user.
   *
   * @param userId the user ID
   * @param user the User object with updated preferences
   * @return the updated User object with preferences
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public User updateUser(int userId, User user) throws IOException, InterruptedException {
    String jsonBody = objectMapper.writeValueAsString(user);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/user/" + userId + "/update"))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    
    // Check if response is successful
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
      return objectMapper.readValue(response.body(), User.class);
    } else {
      throw new IOException("Failed to update user: " + response.body());
    }
  }

  /**
   * Gets user preferences by user ID.
   *
   * @param userId the user ID
   * @return the User object with preferences
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public User getUser(int userId) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), User.class);
  }

  /**
   * Gets the list of all users.
   *
   * @return a list of all User objects
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public List<User> getUserList() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/userList"))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(),
        new TypeReference<List<User>>() {});
  }

  /**
   * Gets weather recommendations for a city.
   *
   * @param city the city name
   * @param days the number of forecast days (1-14)
   * @return the WeatherRecommendation
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public WeatherRecommendation getWeatherRecommendation(String city, int days)
      throws IOException, InterruptedException {
    String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
    String uri = baseUrl + "/recommendation/weather/?city=" + encodedCity + "&days=" + days;

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), WeatherRecommendation.class);
  }

  /**
   * Gets weather alerts by city name.
   *
   * @param city the city name
   * @return the WeatherAlert
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public WeatherAlert getWeatherAlertsByCity(String city)
      throws IOException, InterruptedException {
    String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
    String uri = baseUrl + "/alert/weather?city=" + encodedCity;

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), WeatherAlert.class);
  }

  /**
   * Gets weather alerts by coordinates.
   *
   * @param lat the latitude
   * @param lon the longitude
   * @return the WeatherAlert
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public WeatherAlert getWeatherAlertsByCoordinates(double lat, double lon)
      throws IOException, InterruptedException {
    String uri = baseUrl + "/alert/weather?lat=" + lat + "&lon=" + lon;

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), WeatherAlert.class);
  }

  /**
   * Gets weather alerts for a specific user based on their city preferences.
   *
   * @param userId the user ID
   * @return a list of WeatherAlert objects
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public List<WeatherAlert> getUserWeatherAlerts(int userId)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/alert/weather/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(),
        new TypeReference<List<WeatherAlert>>() {});
  }

  /**
   * Gets crime summary data.
   *
   * @param state the state name or abbreviation
   * @param offense the offense type
   * @param month the month (two-digit format, e.g., "10")
   * @param year the year (four-digit format, e.g., "2025")
   * @return the CrimeSummary
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public CrimeSummary getCrimeSummary(String state, String offense, String month, String year)
      throws IOException, InterruptedException {
    String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);
    String encodedOffense = URLEncoder.encode(offense, StandardCharsets.UTF_8);
    String encodedMonth = URLEncoder.encode(month, StandardCharsets.UTF_8);
    String encodedYear = URLEncoder.encode(year, StandardCharsets.UTF_8);
    String uri = baseUrl + "/crime/summary?state=" + encodedState
        + "&offense=" + encodedOffense
        + "&month=" + encodedMonth
        + "&year=" + encodedYear;

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(uri))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), CrimeSummary.class);
  }

  /**
   * Gets travel advisory for a country.
   *
   * @param country the country name
   * @return the travel advisory as a string
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public String getCountryAdvisory(String country)
      throws IOException, InterruptedException {
    String encodedCountry = URLEncoder.encode(country, StandardCharsets.UTF_8);
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/country/" + encodedCountry))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }

  /**
   * Gets city summary for a city.
   *
   * @param city the city name
   * @return the CitySummary
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public CitySummary getCitySummary(String city)
      throws IOException, InterruptedException {
    return getCitySummary(city, null, null);
  }

  /**
   * Gets city summary for a city with optional date range.
   *
   * @param city the city name
   * @param startDate the start date (optional, format: YYYY-MM-DD)
   * @param endDate the end date (optional, format: YYYY-MM-DD)
   * @return the CitySummary
   * @throws IOException if an I/O error occurs
   * @throws InterruptedException if the request is interrupted
   */
  public CitySummary getCitySummary(String city, String startDate, String endDate)
      throws IOException, InterruptedException {
    String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
    StringBuilder uriBuilder = new StringBuilder(baseUrl + "/summary/" + encodedCity);

    if (startDate != null || endDate != null) {
      uriBuilder.append("?");
      if (startDate != null) {
        uriBuilder.append("startDate=").append(URLEncoder.encode(startDate, StandardCharsets.UTF_8));
      }
      if (endDate != null) {
        if (startDate != null) {
          uriBuilder.append("&");
        }
        uriBuilder.append("endDate=").append(URLEncoder.encode(endDate, StandardCharsets.UTF_8));
      }
    }

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(uriBuilder.toString()))
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return objectMapper.readValue(response.body(), CitySummary.class);
  }
}

