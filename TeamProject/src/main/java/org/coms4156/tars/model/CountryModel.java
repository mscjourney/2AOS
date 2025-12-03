package org.coms4156.tars.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Model that handles the logic that countryInfo.json
 * information, which is general info for a country.
 */
public class CountryModel {

  private JsonNode advisoryArray; // parsed JSON

  /**
   * Default Constructor.
   */
  public CountryModel() {
    loadData();
  }

  /**
   * On init should load the data to be read.
   */
  private void loadData() {
    try {
      String path = "data/countryInfo.json";
      File file = new File(path);

      if (!file.exists()) {
        throw new RuntimeException("JSON not found at: " + file.getAbsolutePath());
      }

      String rawJson = Files.readString(file.toPath());

      ObjectMapper mapper = new ObjectMapper();
      advisoryArray = mapper.readTree(rawJson);

    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to load advisory data", e);
    }
  }

  /**
   * Returns a TravelAdvisory object for a given country.
   */
  public CountrySummary getCountrySummary(String country) {
    if (country == null || country.trim().isEmpty()) {
      throw new IllegalArgumentException("Country cannot be empty.");
    }

    for (JsonNode node : advisoryArray) {
      String nodeCountry = node.path("country").asText(null);

      if (nodeCountry != null && nodeCountry.equalsIgnoreCase(country)) {

        String matchedCountry = nodeCountry;
        String capital = node.path("capital").asText("");
        String summary = node.path("summary").asText("");

        return new CountrySummary(matchedCountry, capital, summary);
      }
    }

    return null;
  }

}
