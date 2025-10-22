package org.coms4156.tars.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@code CrimeModel} class provides functionality for retrieving and processing
 * crime data from the official FBI Crime Data Explorer API.
 *
 * */
public class CrimeModel {

  private static final String BASE_URL =
          "https://api.usa.gov/crime/fbi/cde/summarized/state";
  private static final String API_KEY =
          "mEJVPFVCQAv5qfZKqiegaUPQ7N7bzY28JmCZZtNA"; // Replace if needed

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final Map<String, String> stateMap;

  /**
   * Default constructor.
   * */
  public CrimeModel() {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = new ObjectMapper();
    this.stateMap = createStateMap();
  }

  /**
   * Fetches the FBI crime rate for a given state, offense, and month/year,
   * returning only the formatted string (e.g. "9.57 cases per 100,000 people").
   * Example call:
   *   getCrimeSummary("NC", "ASS", "10", "2025");
   *
   * @param state   The state abbreviation (e.g., "CA", "NC", "TX").
   * @param offense The offense code (e.g., "ASS", "BUR", "HOM", etc.).
   * @param month   The month as a two-digit string (e.g., "10" for October).
   * @param year    The year as a four-digit string (e.g., "2025").
   * @return A human-readable string like "9.57 cases per 100,000 people",
   *         or an error message if data cannot be found.
   */
  public String getCrimeSummary(String state, String offense, String month, String year) {
    String formattedDate = String.format("%s-%s", month, year);
    String url = String.format("%s/%s/%s?from=%s&to=%s&API_KEY=%s",
            BASE_URL, state, offense, formattedDate, formattedDate, API_KEY);

    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .GET()
              .build();

      HttpResponse<String> response = httpClient.send(request,
              HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        throw new RuntimeException("API call failed with status: "
                + response.statusCode());
      }

      JsonNode root = objectMapper.readTree(response.body());
      String fullStateName = getStateName(state);
      JsonNode offenses = root.path("offenses").path("rates").path(fullStateName);

      if (offenses.has(formattedDate)) {
        double rate = offenses.get(formattedDate).asDouble();
        return String.format("%s: %.2f cases per 100,000 people (%s)",
                fullStateName, rate, formattedDate);
      } else {
        return "No rate data available for "
                + fullStateName + " on " + formattedDate;
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
      return "Error fetching data: " + e.getMessage();
    }
  }

  /**
   * Converts a state abbreviation into the full state name
   * to be compatable with the API.
   */
  private String getStateName(String stateAbbrev) {
    return stateMap.getOrDefault(stateAbbrev.toUpperCase(), stateAbbrev);
  }

  /**
   * Populates a map of all 50 U.S. states and D.C.
   */
  private Map<String, String> createStateMap() {
    Map<String, String> map = new HashMap<>();
    map.put("AL", "Alabama");
    map.put("AK", "Alaska");
    map.put("AZ", "Arizona");
    map.put("AR", "Arkansas");
    map.put("CA", "California");
    map.put("CO", "Colorado");
    map.put("CT", "Connecticut");
    map.put("DE", "Delaware");
    map.put("FL", "Florida");
    map.put("GA", "Georgia");
    map.put("HI", "Hawaii");
    map.put("ID", "Idaho");
    map.put("IL", "Illinois");
    map.put("IN", "Indiana");
    map.put("IA", "Iowa");
    map.put("KS", "Kansas");
    map.put("KY", "Kentucky");
    map.put("LA", "Louisiana");
    map.put("ME", "Maine");
    map.put("MD", "Maryland");
    map.put("MA", "Massachusetts");
    map.put("MI", "Michigan");
    map.put("MN", "Minnesota");
    map.put("MS", "Mississippi");
    map.put("MO", "Missouri");
    map.put("MT", "Montana");
    map.put("NE", "Nebraska");
    map.put("NV", "Nevada");
    map.put("NH", "New Hampshire");
    map.put("NJ", "New Jersey");
    map.put("NM", "New Mexico");
    map.put("NY", "New York");
    map.put("NC", "North Carolina");
    map.put("ND", "North Dakota");
    map.put("OH", "Ohio");
    map.put("OK", "Oklahoma");
    map.put("OR", "Oregon");
    map.put("PA", "Pennsylvania");
    map.put("RI", "Rhode Island");
    map.put("SC", "South Carolina");
    map.put("SD", "South Dakota");
    map.put("TN", "Tennessee");
    map.put("TX", "Texas");
    map.put("UT", "Utah");
    map.put("VT", "Vermont");
    map.put("VA", "Virginia");
    map.put("WA", "Washington");
    map.put("WV", "West Virginia");
    map.put("WI", "Wisconsin");
    map.put("WY", "Wyoming");
    map.put("DC", "District of Columbia");
    return map;
  }
}
