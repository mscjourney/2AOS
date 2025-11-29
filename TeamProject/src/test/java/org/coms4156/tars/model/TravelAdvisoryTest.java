package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This class contains the unit tests for TravelAdvisoryModel.
 */
@SpringBootTest
public class TravelAdvisoryTest {

  TravelAdvisoryModel model = new TravelAdvisoryModel();

  @Test
  public void testGetTravelAdvisoryIllegalArgument() {
    IllegalArgumentException ex1 = assertThrows(
          IllegalArgumentException.class,
          () -> model.getTravelAdvisory(null)
      );

    assertEquals("Country cannot be empty.", ex1.getMessage());

    IllegalArgumentException ex2 = assertThrows(
          IllegalArgumentException.class,
          () -> model.getTravelAdvisory("")
      );

    assertEquals("Country cannot be empty.", ex2.getMessage());

    IllegalArgumentException ex3 = assertThrows(
          IllegalArgumentException.class,
          () -> model.getTravelAdvisory("   ")
      );

    assertEquals("Country cannot be empty.", ex3.getMessage());
  }

  @Test
  public void testGetTravelAdvisory() {
    TravelAdvisory mockValue = model.getTravelAdvisory("Japan");

    TravelAdvisory mockJapan = 
          new TravelAdvisory("Japan", "Level 1: Exercise normal precautions", new ArrayList<>());
    
    assertEquals(mockValue.toString(), mockJapan.toString());

    mockValue = model.getTravelAdvisory("Jordan");

    List<String> mockRisks = new ArrayList<>();
    mockRisks.add("Terrorism (T)");
    mockRisks.add("Other (O)");
    TravelAdvisory mockJordan =
          new TravelAdvisory("Jordan", "Level 2: Exercise increased caution", mockRisks);
    
    assertEquals(mockValue.toString(), mockJordan.toString());

    // Testing Invalid Countries
    mockValue = model.getTravelAdvisory("Saturn");
    assertEquals(mockValue, null);
  }
}