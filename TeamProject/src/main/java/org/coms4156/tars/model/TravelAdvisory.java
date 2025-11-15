package org.coms4156.tars.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Travel Advisory object
 */
public class TravelAdvisory {
  private String country;
  private String level;

  @JsonProperty("risk_indicators")
  private ArrayList<String> riskIndicators;

  public TravelAdvisory() {
    this.riskIndicators = new ArrayList<>();
  }

  public TravelAdvisory(String country, String level, ArrayList<String> riskIndicators) {
    this.country = country;
    this.level = level;
    this.riskIndicators = riskIndicators;
  }

  public TravelAdvisory(String country, String level) {
    this.country = country;
    this.level = level;
    this.riskIndicators = new ArrayList<>();
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
    return "country = " + country + "\n" +
            "level = " + level + "\n" +
            "riskIndicators = " + riskIndicators +
            "\n";
  }

}