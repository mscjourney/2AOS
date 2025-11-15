package org.coms4156.tars.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;

public class TravelAdvisoryModel {

  private JsonNode advisoryArray; // parsed JSON

  public TravelAdvisoryModel() {
    loadData();
  }

  private void loadData() {
    try {
      String path = "data/stateDeptAdvisory.json";
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
   * Returns the advisory JSON node for a given country.
   */
  public JsonNode getAdvisoryForCountry(String country) {
    if (country == null || country.trim().isEmpty()) {
      throw new IllegalArgumentException("Country cannot be empty.");
    }

    for (JsonNode node : advisoryArray) {
      if (node.get("country").asText().equalsIgnoreCase(country)) {
        return node;
      }
    }
    return null;
  }

  public TravelAdvisory getTravelAdvisory(String country) {
    if (country == null || country.trim().isEmpty()) {
      throw new IllegalArgumentException("Country cannot be empty.");
    }

    for (JsonNode node : advisoryArray) {
      if (node.get("country").asText().equalsIgnoreCase(country)) {

        String matchedCountry = node.get("country").asText();
        String level = node.get("level").asText();

        ArrayList<String> riskIndicators = new ArrayList<>();
        node.get("risk_indicators").forEach(rn -> riskIndicators.add(rn.asText()));

        return new TravelAdvisory(matchedCountry, level, riskIndicators);
      }
    }

    return null;
  }



}
