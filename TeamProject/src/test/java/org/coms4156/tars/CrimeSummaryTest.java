package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the CrimeSummary model class.
 */
public class CrimeSummaryTest {

  @Test
  void constructorTest() {
    CrimeSummary summary = new CrimeSummary("NC", "10", "2025", "Low crime rate this month.");

    assertEquals("NC", summary.getState());
    assertEquals("10", summary.getmonth());
    assertEquals("2025", summary.getyear());
    assertEquals("Low crime rate this month.", summary.getMessage());
  }

  @Test
  void settersGettersTest() {
    CrimeSummary summary = new CrimeSummary("NY", "01", "2024", "Initial message");

    summary.setState("CA");
    summary.setmonth("12");
    summary.setyear("2025");
    summary.setMessage("Updated summary for testing.");

    assertEquals("CA", summary.getState());
    assertEquals("12", summary.getmonth());
    assertEquals("2025", summary.getyear());
    assertEquals("Updated summary for testing.", summary.getMessage());
  }

  @Test
  void toStringTest() {
    CrimeSummary summary = new CrimeSummary("TX", "05", "2023", "Moderate crime rate.");
    String result = summary.toString();

    assertTrue(result.contains("TX"));
    assertTrue(result.contains("05"));
    assertTrue(result.contains("2023"));
    assertTrue(result.contains("Moderate crime rate."));
    assertTrue(result.startsWith("CrimeSummary"));
  }
}
