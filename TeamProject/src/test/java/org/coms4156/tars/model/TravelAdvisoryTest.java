package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the TravelAdvisory model class.
 */
public class TravelAdvisoryTest {

  private TravelAdvisory travelAdvisory;
  private ArrayList<String> riskIndicators;

  @BeforeEach
  void setUp() {
    riskIndicators = new ArrayList<>();
    riskIndicators.add("Crime (C)");
    riskIndicators.add("Terrorism (T)");
  }

  @Test
  void testDefaultConstructor() {
    travelAdvisory = new TravelAdvisory();
    
    assertNotNull(travelAdvisory);
    assertNotNull(travelAdvisory.getRiskIndicators());
    assertEquals(0, travelAdvisory.getRiskIndicators().size());
  }

  @Test
  void testParameterizedConstructor() {
    travelAdvisory = new TravelAdvisory("United States", "Level 1: Exercise normal precautions", 
                                        riskIndicators);
    
    assertEquals("United States", travelAdvisory.getCountry());
    assertEquals("Level 1: Exercise normal precautions", travelAdvisory.getLevel());
    assertEquals(riskIndicators, travelAdvisory.getRiskIndicators());
    assertEquals(2, travelAdvisory.getRiskIndicators().size());
  }

  @Test
  void testGettersAndSetters() {
    travelAdvisory = new TravelAdvisory();
    
    travelAdvisory.setCountry("Afghanistan");
    travelAdvisory.setLevel("Level 4: Do not travel");
    travelAdvisory.setRiskIndicators(riskIndicators);
    
    assertEquals("Afghanistan", travelAdvisory.getCountry());
    assertEquals("Level 4: Do not travel", travelAdvisory.getLevel());
    assertEquals(riskIndicators, travelAdvisory.getRiskIndicators());
  }

  @Test
  void testSetRiskIndicators() {
    travelAdvisory = new TravelAdvisory();
    
    ArrayList<String> newIndicators = new ArrayList<>();
    newIndicators.add("Health (H)");
    newIndicators.add("Kidnapping or Hostage Taking (K)");
    
    travelAdvisory.setRiskIndicators(newIndicators);
    
    assertEquals(newIndicators, travelAdvisory.getRiskIndicators());
    assertEquals(2, travelAdvisory.getRiskIndicators().size());
  }

  @Test
  void testSetRiskIndicatorsWithEmptyList() {
    travelAdvisory = new TravelAdvisory("Albania", "Level 2: Exercise increased caution", 
                                        riskIndicators);
    
    ArrayList<String> emptyList = new ArrayList<>();
    travelAdvisory.setRiskIndicators(emptyList);
    
    assertEquals(emptyList, travelAdvisory.getRiskIndicators());
    assertEquals(0, travelAdvisory.getRiskIndicators().size());
  }

  @Test
  void testToString() {
    travelAdvisory = new TravelAdvisory("United States", "Level 1: Exercise normal precautions", 
                                        riskIndicators);
    
    String result = travelAdvisory.toString();
    
    assertTrue(result.contains("United States"));
    assertTrue(result.contains("Level 1: Exercise normal precautions"));
    assertTrue(result.contains("Crime (C)"));
    assertTrue(result.contains("Terrorism (T)"));
    assertTrue(result.contains("country ="));
    assertTrue(result.contains("level ="));
    assertTrue(result.contains("riskIndicators ="));
  }

  @Test
  void testToStringWithEmptyRiskIndicators() {
    travelAdvisory = new TravelAdvisory("Andorra", "Level 1: Exercise normal precautions", 
                                        new ArrayList<>());
    
    String result = travelAdvisory.toString();
    
    assertTrue(result.contains("Andorra"));
    assertTrue(result.contains("Level 1: Exercise normal precautions"));
    assertTrue(result.contains("riskIndicators = []"));
  }

  @Test
  void testMultipleRiskIndicators() {
    ArrayList<String> multipleIndicators = new ArrayList<>();
    multipleIndicators.add("Unrest (U)");
    multipleIndicators.add("Crime (C)");
    multipleIndicators.add("Health (H)");
    multipleIndicators.add("Kidnapping or Hostage Taking (K)");
    multipleIndicators.add("Terrorism (T)");
    multipleIndicators.add("Wrongful Detention (D)");
    
    travelAdvisory = new TravelAdvisory("Afghanistan", "Level 4: Do not travel", 
                                        multipleIndicators);
    
    assertEquals(6, travelAdvisory.getRiskIndicators().size());
    assertTrue(travelAdvisory.getRiskIndicators().contains("Unrest (U)"));
    assertTrue(travelAdvisory.getRiskIndicators().contains("Wrongful Detention (D)"));
  }
}

