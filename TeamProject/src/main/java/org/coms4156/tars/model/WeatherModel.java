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
        return "Could not find coordinates for city: " + city;
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
   */
  public static WeatherRecommendation getRecommendedDays(String city, int days) {
    String forecast = getWeatherForCity(city, days);
    List<String> niceDays = new ArrayList<>();

    try {
      String[] dates = forecast.split("\"time\":\\[")[1]
              .split("]")[0]
              .replace("\"", "").split(",");
      String[] codes = forecast.split("\"weathercode\":\\[")[1]
              .split("]")[0]
              .split(",");
      // Min/max temperature are pulled in the JSON but not yet implemented here

      for (int i = 0; i < dates.length && i < codes.length; i++) {
        int code = Integer.parseInt(codes[i].trim());

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

      return new WeatherRecommendation(city, niceDays, message);

    } catch (Exception e) {
      return new WeatherRecommendation(city, List.of(),
              "Error processing forecast: " + e.getMessage());
    }
  }


}




