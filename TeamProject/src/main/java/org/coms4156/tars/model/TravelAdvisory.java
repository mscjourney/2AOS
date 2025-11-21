package org.coms4156.tars.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

/**
 * Travel Advisory object.
 */
public class TravelAdvisory {
  private String country;
  private String level;

  @JsonProperty("risk_indicators")
  private ArrayList<String> riskIndicators;

  /**
   * Default constructor.
   */
  public TravelAdvisory() {
    this.riskIndicators = new ArrayList<>();
  }

  /**
   * Complete constuctor for the TravelAdvisory object.
   */
  public TravelAdvisory(String country, String level, ArrayList<String> riskIndicators) {
    this.country = country;
    this.level = level;
    this.riskIndicators = riskIndicators;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public ArrayList<String> getRiskIndicators() {
    return riskIndicators;
  }

  public void setRiskIndicators(ArrayList<String> riskIndicators) {
    this.riskIndicators = riskIndicators;
  }

  @Override
  public String toString() {
    return "country = " + country + "\n"
            + "level = " + level + "\n"
            + "riskIndicators = " + riskIndicators
            + "\n";
  }

}