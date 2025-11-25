package org.coms4156.tars.model;

/**
 * Crime Summary object. Stores the state, month, year, and a summary
 * for associated crime activity.
 */
public class CrimeSummary {
  private String state;
  private String month;
  private String year;
  private String message;

  /**
   *
   */
  public CrimeSummary() {
  }

  /**
   * Constructs a new CrimeSummary instance with the specified state, month, year, and message.
   *
   * @param state   the U.S. state name or abbreviation (e.g., "North Carolina" or "NC")
   * @param month   the month of the crime data in two-digit format (e.g., "10" for October)
   * @param year    the year of the crime data in four-digit format (e.g., "2025")
   * @param message a descriptive summary or formatted message of the crime statistics
   */
  public CrimeSummary(String state, String month, String year, String message) {
    this.state = state;
    this.month = month;
    this.year = year;
    this.message = message;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "CrimeSummary {"
            + "state='" + state + '\''
            + ", month='" + month + '\''
            + ", year='" + year + '\''
            + ", message='" + message + '\''
            + '}';
  }
}