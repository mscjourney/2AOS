package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for model classes that exercise model interactions
 * and shared data structures.
 * 
 * <p>These tests verify:
 * 1. Model data sharing and consistency
 * 2. Model serialization/deserialization compatibility
 * 3. Model relationships and references
 */
public class ModelIntegrationTest {

  /**
   * Integration test: TarsUser and UserPreference relationship via userId.
   * Verifies model ID relationship consistency.
   */
  @Test
  public void testTarsUserUserPreferenceRelationship() {
    // Create TarsUser
    TarsUser tarsUser = new TarsUser(1L, "test_user", "test@user.com", "user");
    tarsUser.setUserId(100L);
    Long userId = tarsUser.getUserId();

    // Create UserPreference with matching userId
    UserPreference userPreference = new UserPreference(
        userId,
        List.of("sunny"),
        List.of("25"),
        List.of("Miami")
    );

    // Verify relationship
    assertEquals(userId, userPreference.getId());
    assertEquals(100L, userPreference.getId());
    assertNotNull(tarsUser.getClientId());
    assertEquals(1L, tarsUser.getClientId());
  }

  /**
   * Integration test: Client and TarsUser relationship via clientId.
   * Verifies model ID relationship consistency.
   */
  @Test
  public void testClientTarsUserRelationship() {
    // Create Client
    Client client = new Client(50L, "TestClient", "client@test.com", "api-key-123");
    Long clientId = client.getClientId();

    // Create TarsUser with matching clientId
    TarsUser tarsUser = new TarsUser(clientId, "client_user", "user@test.com", "admin");
    tarsUser.setUserId(200L);

    // Verify relationship
    assertEquals(clientId, tarsUser.getClientId());
    assertEquals(50L, tarsUser.getClientId());
    assertEquals(50L, client.getClientId());
  }

  /**
   * Integration test: Multiple UserPreferences with same city.
   * Verifies UserPreference data sharing and consistency.
   */
  @Test
  public void testMultipleUserPreferencesForSameCity() {
    String cityName = "Boston";

    // Create multiple user preferences for the same city
    UserPreference user1 = new UserPreference(
        1L, List.of("sunny"), List.of("20"), List.of(cityName));
    UserPreference user2 = new UserPreference(
        2L, List.of("rainy"), List.of("15"), List.of(cityName, "New York"));
    UserPreference user3 = new UserPreference(
        3L, List.of("snowy"), List.of("0"), List.of(cityName));

    // Verify all users have the city in their preferences
    assertTrue(user1.getCityPreferences().contains(cityName));
    assertTrue(user2.getCityPreferences().contains(cityName));
    assertTrue(user3.getCityPreferences().contains(cityName));
    
    // Verify users can share same city preference
    assertEquals(cityName, user1.getCityPreferences().get(0));
    assertTrue(user2.getCityPreferences().contains(cityName));
    assertEquals(cityName, user3.getCityPreferences().get(0));
  }

  /**
   * Integration test: WeatherAlert and WeatherRecommendation consistency.
   * Verifies related weather models share location data.
   */
  @Test
  public void testWeatherModelsLocationConsistency() {
    String location = "Seattle";

    // Create WeatherRecommendation
    final WeatherRecommendation weatherRec = new WeatherRecommendation(
        location,
        List.of("2024-06-15"),
        "Cloudy with light rain",
        16.0,
        20.0
    );

    // Create WeatherAlert for same location
    final List<Map<String, String>> alerts = new ArrayList<>();
    Map<String, String> alert = new HashMap<>();
    alert.put("severity", "low");
    alert.put("type", "rain");
    alert.put("message", "Light rain expected");
    alerts.add(alert);
    
    WeatherAlert weatherAlert = new WeatherAlert(
        location,
        alerts,
        List.of("Carry an umbrella"),
        Map.of("temperature", 18.0, "condition", "cloudy")
    );

    // Verify location consistency
    assertEquals(location, weatherRec.getCity());
    assertEquals(location, weatherAlert.getLocation());
    
    // Verify weather data consistency
    assertEquals(16.0, weatherRec.getMinTemperature());
    assertEquals(20.0, weatherRec.getMaxTemperature());
    double alertTemp = ((Number) weatherAlert.getCurrentConditions()
        .get("temperature")).doubleValue();
    assertEquals(18.0, alertTemp);
  }

  /**
   * Integration test: TravelAdvisory country relationship.
   * Verifies country-level model relationships.
   */
  @Test
  public void testTravelAdvisoryCountryRelationship() {
    String country = "France";

    // Create TravelAdvisory
    List<String> riskIndicators = new ArrayList<>();
    riskIndicators.add("Terrorism");
    riskIndicators.add("Civil unrest");
    TravelAdvisory travelAdvisory = new TravelAdvisory(country, "Level 2", riskIndicators);

    // Verify country relationship
    assertEquals(country, travelAdvisory.getCountry());
    assertEquals("Level 2", travelAdvisory.getLevel());
    assertEquals(2, travelAdvisory.getRiskIndicators().size());
    assertTrue(travelAdvisory.getRiskIndicators().contains("Terrorism"));
  }

  /**
   * Integration test: Complete model chain - Client -> TarsUser -> UserPreference.
   * Verifies end-to-end model relationships.
   */
  @Test
  public void testCompleteModelChain() {
    // Step 1: Create Client
    Client client = new Client(1L, "TravelAgency", "agency@test.com", "api-key");
    Long clientId = client.getClientId();

    // Step 2: Create TarsUser for client
    TarsUser tarsUser = new TarsUser(clientId, "traveler", "traveler@test.com", "user");
    tarsUser.setUserId(500L);
    Long userId = tarsUser.getUserId();

    // Step 3: Create UserPreference for user
    UserPreference userPreference = new UserPreference(
        userId,
        List.of("sunny", "warm"),
        List.of("25", "30"),
        List.of("Barcelona", "Madrid")
    );

    // Verify complete chain
    assertEquals(clientId, tarsUser.getClientId());
    assertEquals(userId, userPreference.getId());
    assertTrue(userPreference.getCityPreferences().contains("Barcelona"));
    assertTrue(userPreference.getCityPreferences().contains("Madrid"));
  }

  /**
   * Integration test: Model equals and hashCode consistency.
   * Verifies model identity relationships work correctly.
   */
  @Test
  public void testModelIdentityRelationships() {
    // Test Client equality
    Client client1 = new Client(10L, "Client1", "client1@test.com", "key1");
    Client client2 = new Client(10L, "Client2", "client2@test.com", "key2");
    assertEquals(client1, client2, "Clients with same ID should be equal");

    // Test UserPreference equality
    UserPreference pref1 = new UserPreference(
        20L, List.of("sunny"), List.of("25"), List.of("NYC"));
    UserPreference pref2 = new UserPreference(
        20L, List.of("rainy"), List.of("15"), List.of("Boston"));
    assertEquals(pref1, pref2, "UserPreferences with same ID should be equal");

    // Test in collection
    List<UserPreference> prefs = new ArrayList<>();
    prefs.add(pref1);
    assertTrue(prefs.contains(pref2), "Collection should find equal preference");
  }
}

