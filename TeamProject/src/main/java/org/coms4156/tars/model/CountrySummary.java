package org.coms4156.tars.model;

/**
 * {@code CountrySummary} Constructs a CountrySummary with the specified parameters.
 *
 * @param country the country name
 * @param capital the the capital of the country
 * @param travelAdvisory the travel advisory for the city
 * @param message the message associated with the country summary
 */
public class CountrySummary {
  private String country;
  private String capital;
  private TravelAdvisory travelAdvisory;
  private String message;

  CountrySummary(String country, String capital, TravelAdvisory travelAdvisory, String message) {
    this.country = country;
    this.capital = capital;
    this.travelAdvisory = travelAdvisory;
    this.message = message;
  }

  CountrySummary(String country, String capital, String message) {
    this.country = country;
    this.capital = capital;
    this.travelAdvisory = null;
    this.message = message;
  }

  CountrySummary() {
    this.country = "";
    this.capital = "";
    this.travelAdvisory = null;
    this.message = "";
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getCapital() {
    return capital;
  }

  public void setCapital(String capital) {
    this.capital = capital;
  }

  public TravelAdvisory getTravelAdvisory() {
    return travelAdvisory;
  }

  public void setTravelAdvisory(TravelAdvisory travelAdvisory) {
    this.travelAdvisory = travelAdvisory;
  }

  public String getMessage() {
    return message;
  }


  @Override
  public String toString() {
    return "CountrySummary {"
            + "country='" + country + '\''
            + ", capital='" + capital
            + ", travelAdvisory=" + travelAdvisory
            + ", message='" + message + '\''
            + '}';
  }

}