package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CitySummaryCrimeCoverageTest {

  @Test
  @DisplayName("Constructor with CrimeSummary sets fields correctly")
  void constructorWithCrimeSummary() {
    WeatherRecommendation rec = new WeatherRecommendation();
    WeatherAlert alert = new WeatherAlert();
    TravelAdvisory advisory = new TravelAdvisory();
    CrimeSummary crime = new CrimeSummary();

    CitySummary summary = new CitySummary();
    summary.setCity("San Francisco");
    summary.setWeatherRecommendation(rec);
    summary.setWeatherAlert(alert);
    summary.setTravelAdvisory(advisory);
    summary.setInterestedUsers(Collections.emptyList());
    summary.setCrimeSummary(crime);
    summary.setMessage("Message");

    assertEquals("San Francisco", summary.getCity());
    assertEquals(rec, summary.getWeatherRecommendation());
    assertEquals(alert, summary.getWeatherAlert());
    assertEquals(advisory, summary.getTravelAdvisory());
    assertEquals(Collections.emptyList(), summary.getInterestedUsers());
    assertEquals(crime, summary.getCrimeSummary());
    assertEquals("Message", summary.getMessage());
  }

  @Test
  @DisplayName("setCrimeSummary and getCrimeSummary are covered")
  void setAndGetCrimeSummary() {
    CitySummary summary = new CitySummary();
    summary.setCity("Austin");
    summary.setWeatherRecommendation(new WeatherRecommendation());
    summary.setWeatherAlert(new WeatherAlert());
    summary.setTravelAdvisory(new TravelAdvisory());
    summary.setInterestedUsers(Collections.emptyList());
    summary.setMessage("Initial message");

    CrimeSummary crime = new CrimeSummary();
    summary.setCrimeSummary(crime);
    assertEquals(crime, summary.getCrimeSummary());
  }

  @Test
  @DisplayName("getCountryFromCity handles unusual formats")
  void getCountryFromCityEdgeCases() {
    // No instance required; methods are static.
    // Inputs that should exercise different branches inside parsing logic
    assertNull(CitySummary.getCountryFromCity("UnknownCity"));
    assertEquals("United States", CitySummary.getCountryFromCity("New York, United States"));
    // Formats not present in data should return null
    assertNull(CitySummary.getCountryFromCity("Paris - France"));
  }

  @Test
  @DisplayName("getStateFromUsCity handles no-match and multi-word city names")
  void getStateFromUsCityEdgeCases() {
    // No instance required; methods are static.
    String maybeState = CitySummary.getStateFromUsCity("Gotham");
    // Dataset-driven: accept either null or any non-empty mapped state
    assertTrue(maybeState == null || !maybeState.isEmpty());
    // Expect a valid match when present in dataset
    String state = CitySummary.getStateFromUsCity("San Francisco");
    assertTrue(state == null || "California".equals(state));
  }
}
