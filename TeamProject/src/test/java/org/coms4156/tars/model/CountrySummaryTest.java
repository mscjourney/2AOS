package org.coms4156.tars.model;

import org.coms4156.tars.model.TravelAdvisory;
import org.coms4156.tars.model.TravelAdvisoryModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the CountrySummary model class.
 */
public class CountrySummaryTest {

  @Test
  void constructorTest() {
    TravelAdvisory advisory = new TravelAdvisory();
    CountrySummary summary = new CountrySummary("Japan", "Tokyo", advisory, "Safe to travel.");

    assertEquals("Japan", summary.getCountry());
    assertEquals("Tokyo", summary.getCapital());
    assertEquals(advisory, summary.getTravelAdvisory());
    assertEquals("Safe to travel.", summary.getMessage());
  }

  @Test
  void constructorWithoutAdvisoryTest() {
    CountrySummary summary = new CountrySummary("France", "Paris", "Enjoy your stay!");

    assertEquals("France", summary.getCountry());
    assertEquals("Paris", summary.getCapital());
    assertNull(summary.getTravelAdvisory());
    assertEquals("Enjoy your stay!", summary.getMessage());
  }

  @Test
  void defaultConstructorTest() {
    CountrySummary summary = new CountrySummary();

    assertEquals("", summary.getCountry());
    assertEquals("", summary.getCapital());
    assertNull(summary.getTravelAdvisory());
    assertEquals("", summary.getMessage());
  }

  @Test
  void settersGettersTest() {
    CountrySummary summary = new CountrySummary();

    TravelAdvisory advisory = new TravelAdvisory();

    summary.setCountry("Canada");
    summary.setCapital("Ottawa");
    summary.setTravelAdvisory(advisory);
    summary.setMessage("Updated message");

    assertEquals("Canada", summary.getCountry());
    assertEquals("Ottawa", summary.getCapital());
    assertEquals(advisory, summary.getTravelAdvisory());
    assertEquals("Updated message", summary.getMessage());
  }

  @Test
  void toStringTest() {
    TravelAdvisoryModel model = new TravelAdvisoryModel();
    TravelAdvisory advisory = model.getTravelAdvisory("Germany");
    CountrySummary summary = new CountrySummary("Germany", "Berlin", advisory, "Be cautious.");

    String result = summary.toString();

    assertTrue(result.contains("Germany"));
    assertTrue(result.contains("Berlin"));
    assertTrue(result.contains("Level 2"));
    assertTrue(result.startsWith("CountrySummary"));
  }
}