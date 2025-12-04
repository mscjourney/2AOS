package org.coms4156.tars.model;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles weather forecast data retrieval and recommendations.
 */
public class WeatherModel {

  private static final HttpClient client = HttpClient.newHttpClient();

  /**
   * Helper function that calls the weather API and gets forecast information.
   */
  private static String getWeatherForCity(String city, int days) {
    try {
      // Get city coordinates
      String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name="
              + city.replace(" ", "%20");
      HttpRequest geoRequest = HttpRequest.newBuilder()
              .uri(URI.create(geoUrl))
              .GET()
              .build();

      HttpResponse<String> geoResponse = client.send(geoRequest,
              HttpResponse.BodyHandlers.ofString());

      // Parse coordinates
      String body = geoResponse.body();
      int latIndex = body.indexOf("\"latitude\":");
      int lonIndex = body.indexOf("\"longitude\":");
      if (latIndex == -1 || lonIndex == -1) {
        return "City Not Found";
      }

      double latitude = Double.parseDouble(body.substring(latIndex + 11,
              body.indexOf(",", latIndex)).trim());
      double longitude = Double.parseDouble(body.substring(lonIndex + 12,
              body.indexOf(",", lonIndex)).trim());

      // Gets the dates the number of days specified, max temp, min temp, and forecast code
      String forecastUrl = String.format(
              "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f"
                      + "&daily=weathercode,temperature_2m_max,temperature_2m_min"
                      + "&forecast_days=%d&timezone=auto",
              latitude, longitude, days
      );

      HttpRequest weatherRequest = HttpRequest.newBuilder()
              .uri(URI.create(forecastUrl))
              .GET()
              .build();

      HttpResponse<String> weatherResponse = client.send(weatherRequest,
              HttpResponse.BodyHandlers.ofString());

      return weatherResponse.body();

    } catch (IOException | InterruptedException e) {
      return "Error fetching weather data: " + e.getMessage();
    }
  }

  /**
   * Calls getWeatherForCity() and returns the days for clear skies
   * as a WeatherRecommendation Object.
   * 
   * @param city the city that we are generating recommendation for
   * @param days the range of days taken into consideration. Must be in range [1, 14] inclusively
   * @return a WeatherRecommendation Object on success. Throws IllegalArgumentException if
   *    city does not exist (based on external API) or days out of range. Throws RuntimeException
   *    on API failure or if parsing failed due to malformed json body.
   */
  public static WeatherRecommendation getRecommendedDays(String city, int days) {
    if (days <= 0 || days > 14) {
      throw new IllegalArgumentException("Days out of range");
    }
    
    String forecast = getWeatherForCity(city, days);

    if (forecast.equals("City Not Found")) { // Handle Invalid City Cases
      throw new IllegalArgumentException("City Not Found");
    } else if (forecast.contains("Error fetching weather data:")) {
      throw new RuntimeException("API Failure");
    }
    List<String> niceDays = new ArrayList<>();

    try {
      // Parse dates
      String[] dates = forecast.split("\"time\":\\[")[1]
              .split("]")[0]
              .replace("\"", "")
              .split(",");

      // Parse weather codes
      String[] codes = forecast.split("\"weathercode\":\\[")[1]
              .split("]")[0]
              .split(",");

      // Parse max temperatures
      String[] maxTempsStr = forecast.split("\"temperature_2m_max\":\\[")[1]
              .split("]")[0]
              .split(",");

      // Parse min temperatures
      String[] minTempsStr = forecast.split("\"temperature_2m_min\":\\[")[1]
              .split("]")[0]
              .split(",");

      double absoluteMaxTemp = Double.parseDouble(maxTempsStr[0]);
      double absoluteMinTemp = Double.parseDouble(minTempsStr[0]);

      for (int i = 0; i < dates.length && i < codes.length; i++) {
        int code = Integer.parseInt(codes[i].trim());
        double maxTemp = Double.parseDouble(maxTempsStr[i].trim());
        double minTemp = Double.parseDouble(minTempsStr[i].trim());

        if (absoluteMaxTemp < maxTemp) {
          absoluteMaxTemp = maxTemp;
        }

        if (absoluteMinTemp > minTemp) {
          absoluteMinTemp = minTemp;
        }

        if (code >= 0 && code <= 3) {
          niceDays.add(dates[i].trim());
        }
      }

      String message;
      if (niceDays.isEmpty()) {
        message = "No clear days in the next "
                + days + " days for " + city + ".";
      } else {
        message = "These days are expected to have clear weather in "
                + city + "!";
      }

      return new WeatherRecommendation(city, niceDays, message, absoluteMinTemp, absoluteMaxTemp);

    } catch (Exception e) {
      throw new RuntimeException("Parsing Failure"); // API returned malformed body.
    }
  }

  /**
   * Calls getWeatherForCity() and returns the days for clear skies
   * and preferred temperature as a WeatherRecommendation Object for a specific user
   * 
   * @param city the city that we are generating recommendation for
   * @param days the range of days taken into consideration. Must be in range [1, 14] inclusively
   * @param user the user who we are generating for using their temperature preferences.
   *      Can never be called with null UserPreference.
   * @return a WeatherRecommendation Object on success. Throws IllegalArgumentException if
   *    city does not exist (based on external API) or days out of range. Throws RuntimeException
   *    on API failure or if parsing failed due to malformed json body.
   */
  public static WeatherRecommendation getUserRecDays(String city, int days, UserPreference user) {
    if (days <= 0 || days > 14) {
      throw new IllegalArgumentException("Days out of range");
    }
    
    String forecast = getWeatherForCity(city, days);

    if (forecast.equals("City Not Found")) { // Handle Invalid City Cases
      throw new IllegalArgumentException("City Not Found");
    } else if (forecast.contains("Error fetching weather data:")) {
      throw new RuntimeException("API Failure");
    }

    List<String> niceDays = new ArrayList<>();

    try {
      // Parse dates
      String[] dates = forecast.split("\"time\":\\[")[1]
              .split("]")[0]
              .replace("\"", "")
              .split(",");

      // Parse weather codes
      String[] codes = forecast.split("\"weathercode\":\\[")[1]
              .split("]")[0]
              .split(",");

      // Parse max temperatures
      String[] maxTempsStr = forecast.split("\"temperature_2m_max\":\\[")[1]
              .split("]")[0]
              .split(",");

      // Parse min temperatures
      String[] minTempsStr = forecast.split("\"temperature_2m_min\":\\[")[1]
              .split("]")[0]
              .split(",");

      // Evaluate each day
      List<String> tempPrefs = user.getTemperaturePreferences();

      double absoluteMaxTemp = Double.parseDouble(maxTempsStr[0]);
      double absoluteMinTemp = Double.parseDouble(minTempsStr[0]);

      for (int i = 0; i < dates.length; i++) {
        double maxTemp = Double.parseDouble(maxTempsStr[i].trim());
        double minTemp = Double.parseDouble(minTempsStr[i].trim());

        boolean tempMatches = false;
        for (String tempPref : tempPrefs) {
          try {
            double prefTemp = Double.parseDouble(tempPref.trim());
            if (prefTemp >= minTemp && prefTemp <= maxTemp) {
              tempMatches = true;
              break;
            }
          } catch (NumberFormatException ignored) {
            // Skip invalid
          }
        }

        if (absoluteMaxTemp < maxTemp) {
          absoluteMaxTemp = maxTemp;
        }

        if (absoluteMinTemp > minTemp) {
          absoluteMinTemp = minTemp;
        }

        int code = Integer.parseInt(codes[i].trim());
        boolean isClear = (code >= 0 && code <= 3);

        if (isClear && tempMatches) {
          niceDays.add(dates[i].trim());
        }
      }

      String message = niceDays.isEmpty()
              ? "No days meet your preferences for user " + user.getId()
              + " in " + city + " over the next " + days + " days."
              : "Recommended days for user " + user.getId()
              + " based on clear weather and temperature preferences!";

      return new WeatherRecommendation(city, niceDays, message, absoluteMinTemp, absoluteMaxTemp);

    } catch (Exception e) {
      throw new RuntimeException("Parsing Failure"); // API returned malformed body.
    }
  }


}




