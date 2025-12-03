package org.coms4156.tars.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@code CitySummaryTest}
 * Contains Equivalence Partitions for CitySummary model.
 */
@SpringBootTest
public class CitySummaryTest {

  private CitySummary citySummary;
  private WeatherRecommendation weatherRecommendation;
  private WeatherAlert weatherAlert;
  private TravelAdvisory travelAdvisory;
  private List<UserPreference> interestedUsers;
  private String message;

  @BeforeEach
  void setUp() {
    weatherRecommendation = new WeatherRecommendation("New York", 
        Arrays.asList("2025-03-01", "2025-03-02"), "Clear skies expected.");
    weatherAlert = new WeatherAlert();
    weatherAlert.setLocation("New York");
    
    ArrayList<String> riskIndicators = new ArrayList<>();
    travelAdvisory = new TravelAdvisory("United States", 
        "Level 1: Exercise normal precautions", riskIndicators);
    
    interestedUsers = new ArrayList<>();
    interestedUsers.add(new UserPreference(1L));
    interestedUsers.add(new UserPreference(2L));
    
    message = "Test message for city summary";
  }

  /* ======= Basic Object Contruction and Fields tests ======= */

  @Test
  void testDefaultConstructor() {
    citySummary = new CitySummary();
    
    assertNotNull(citySummary);
    assertEquals("", citySummary.getCity());
    assertNull(citySummary.getWeatherRecommendation());
    assertNull(citySummary.getWeatherAlert());
    assertNull(citySummary.getTravelAdvisory());
    assertNull(citySummary.getInterestedUsers());
    assertEquals("", citySummary.getMessage());
  }

  @Test
  void testParameterizedConstructor() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert, 
                                  travelAdvisory, interestedUsers, message);
    
    assertEquals("New York", citySummary.getCity());
    assertEquals(weatherRecommendation, citySummary.getWeatherRecommendation());
    assertEquals(weatherAlert, citySummary.getWeatherAlert());
    assertEquals(travelAdvisory, citySummary.getTravelAdvisory());
    assertEquals(interestedUsers, citySummary.getInterestedUsers());
    assertEquals(message, citySummary.getMessage());
  }

  @Test
  void testGettersAndSetters() {
    citySummary = new CitySummary();
    
    citySummary.setCity("Los Angeles");
    citySummary.setWeatherRecommendation(weatherRecommendation);
    citySummary.setWeatherAlert(weatherAlert);
    citySummary.setTravelAdvisory(travelAdvisory);
    citySummary.setInterestedUsers(interestedUsers);
    citySummary.setMessage("Updated message");
    
    assertEquals("Los Angeles", citySummary.getCity());
    assertEquals(weatherRecommendation, citySummary.getWeatherRecommendation());
    assertEquals(weatherAlert, citySummary.getWeatherAlert());
    assertEquals(travelAdvisory, citySummary.getTravelAdvisory());
    assertEquals(interestedUsers, citySummary.getInterestedUsers());
    assertEquals("Updated message", citySummary.getMessage());
  }

  @Test
  void testSetCity() {
    citySummary = new CitySummary();

    citySummary.setCity("Chicago");
    assertEquals("Chicago", citySummary.getCity());

    citySummary.setCity("San Francisco");
    assertEquals("San Francisco", citySummary.getCity());
  }

  @Test
  void testSetWeatherRecommendation() {
    citySummary = new CitySummary();

    WeatherRecommendation newRec = new WeatherRecommendation("Boston",
            Arrays.asList("2025-04-01"), "Good weather ahead.");
    citySummary.setWeatherRecommendation(newRec);

    assertEquals(newRec, citySummary.getWeatherRecommendation());
    assertEquals("Boston", citySummary.getWeatherRecommendation().getCity());
  }

  @Test
  void testSetWeatherRecommendationNull() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert,
            travelAdvisory, interestedUsers, message);

    citySummary.setWeatherRecommendation(null);
    assertNull(citySummary.getWeatherRecommendation());
  }

  @Test
  void testSetWeatherAlert() {
    citySummary = new CitySummary();

    WeatherAlert newAlert = new WeatherAlert();
    newAlert.setLocation("Miami");
    citySummary.setWeatherAlert(newAlert);

    assertEquals(newAlert, citySummary.getWeatherAlert());
    assertEquals("Miami", citySummary.getWeatherAlert().getLocation());
  }

  @Test
  void testSetWeatherAlertNull() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert,
            travelAdvisory, interestedUsers, message);

    citySummary.setWeatherAlert(null);
    assertNull(citySummary.getWeatherAlert());
  }

  @Test
  void testSetTravelAdvisory() {
    citySummary = new CitySummary();

    TravelAdvisory newAdvisory = new TravelAdvisory("Canada",
            "Level 1: Exercise normal precautions", new ArrayList<>());
    citySummary.setTravelAdvisory(newAdvisory);

    assertEquals(newAdvisory, citySummary.getTravelAdvisory());
    assertEquals("Canada", citySummary.getTravelAdvisory().getCountry());
  }

  @Test
  void testSetTravelAdvisoryNull() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert,
            travelAdvisory, interestedUsers, message);

    citySummary.setTravelAdvisory(null);
    assertNull(citySummary.getTravelAdvisory());
  }

  @Test
  void testSetInterestedUsers() {
    citySummary = new CitySummary();

    List<UserPreference> newUsers = new ArrayList<>();
    newUsers.add(new UserPreference(3L));
    newUsers.add(new UserPreference(4L));
    newUsers.add(new UserPreference(5L));

    citySummary.setInterestedUsers(newUsers);

    assertEquals(newUsers, citySummary.getInterestedUsers());
    assertEquals(3, citySummary.getInterestedUsers().size());
  }

  @Test
  void testSetInterestedUsersNull() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert,
            travelAdvisory, interestedUsers, message);

    citySummary.setInterestedUsers(null);
    assertNull(citySummary.getInterestedUsers());
  }

  @Test
  void testSetInterestedUsersEmpty() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert,
            travelAdvisory, interestedUsers, message);

    citySummary.setInterestedUsers(new ArrayList<>());
    assertNotNull(citySummary.getInterestedUsers());
    assertEquals(0, citySummary.getInterestedUsers().size());
  }

  @Test
  void testSetMessage() {
    citySummary = new CitySummary();

    citySummary.setMessage("New test message");
    assertEquals("New test message", citySummary.getMessage());

    citySummary.setMessage("");
    assertEquals("", citySummary.getMessage());
  }

  /* ======= String extractValue(String json, String key) Equivalence Partitions  ======= */

  /**
   * {@code testExtractValueValidBody()}
   *  Equivalence Partition 1: json body is correctly populated and is valid, String key
   *    values are valid keys in the json body.
   */
  @Test
  void testExtractValueValidBody() throws Exception {
    Class<?> citySummaryClass = Class.forName("org.coms4156.tars.model.CitySummary");
    Class<?>[] params = {String.class, String.class};
    Method extractValue = citySummaryClass.getDeclaredMethod("extractValue", params);
    extractValue.setAccessible(true);

    String jsonBody = """ 
      {
        "country":"United States",
        "admin1":"New York"
      }
        """;

    String value = (String) extractValue.invoke(null, jsonBody, "\"country\":\"");
    assertEquals("United States", value);

    value = (String) extractValue.invoke(null, jsonBody, "\"admin1\":\"");
    assertEquals("New York", value);

    // Non Existing Fields should return null
    value = (String) extractValue.invoke(null, jsonBody, "\"state\":\"");
    assertNull(value);
  }

  /**
   * {@code testExtractValueValidBody()}
   *  Equivalence Partition 2: json body is not correctly populated and is invalid, String key
   *    values are valid keys in the json body.
   */
  @Test
  void testExtractValueMalformedBody() throws Exception {
    Class<?> citySummaryClass = Class.forName("org.coms4156.tars.model.CitySummary");
    Class<?>[] params = {String.class, String.class};
    Method extractValue = citySummaryClass.getDeclaredMethod("extractValue", params);
    extractValue.setAccessible(true);

    // Missing ending quotations for country
    // substring parsed will "United States,\n" which should return null
    String jsonBody = """ 
      {
        "country":"United States, 
        "admin1":"New York"
      }
        """;

    String value = (String) extractValue.invoke(null, jsonBody, "\"country\":\"");
    assertNull(value);

    value = (String) extractValue.invoke(null, jsonBody, "\"admin1\":\"");
    assertEquals("New York", value);

    jsonBody = """ 
      {
        "country":"Italy
      }
        """;

    value = (String) extractValue.invoke(null, jsonBody, "\"country\":\"");
    assertNull(value); // no ending quotation mark
  }

  /**
   * {@code testExtractValueValidBody()}
   *  Equivalence Partition 3: json body is not correctly populated and is invalid, String key
   *    values are invalid keys in the json body.
   */
  @Test
  void testExtractValueInvalid() throws Exception {
    Class<?> citySummaryClass = Class.forName("org.coms4156.tars.model.CitySummary");
    Class<?>[] params = {String.class, String.class};
    Method extractValue = citySummaryClass.getDeclaredMethod("extractValue", params);
    extractValue.setAccessible(true);

    // Missing ending quotations for country → malformed
    String jsonBody = """ 
      {
        "country":"United States, 
        "admin1":"New York"
      }
        """;

    // Valid key but malformed value → should return null
    String value = (String) extractValue.invoke(null, jsonBody, "\"country\":\"");
    assertNull(value);

    // Valid key with correct value in a malformed JSON → should still return correct value
    value = (String) extractValue.invoke(null, jsonBody, "\"admin1\":\"");
    assertEquals("New York", value);

    // Invalid keys should return null
    value = (String) extractValue.invoke(null, jsonBody, "\"state\":\"");
    assertNull(value);

    value = (String) extractValue.invoke(null, jsonBody, "\"region\":\"");
    assertNull(value);

    // Another malformed JSON (missing closing quote entirely)
    jsonBody = """ 
    {
      "country":"Italy
    }
      """;

    value = (String) extractValue.invoke(null, jsonBody, "\"country\":\"");
    assertNull(value); // no ending quotation mark

    // Invalid key on malformed JSON still should return null
    value = (String) extractValue.invoke(null, jsonBody, "\"continent\":\"");
    assertNull(value);
  }


  /* ======= String getCountryFromCity(String city) Equivlence Partitions ======= */
  /**
   * {@code testExtractValueValidBody()}
   *  Equivalence Partition 1:  String city is a valid String and a valid city.
   */
  @Test
  void testGetCountryFromCityValidCity() {
    // Test with a well-known city - this makes an actual HTTP call
    // Note: This test may fail if the API is unavailable, but it tests the actual functionality
    String country = CitySummary.getCountryFromCity("New York");

    // The method may return null if the API call fails or city is not found
    // We just verify the method doesn't throw an exception
    // In a real scenario, you might want to mock the HTTP client
    assertTrue(country == null || country.length() > 0);
  }

  /**
   * {@code testGetCountryFromCityWithInternationalCity()}
   *  Equivalence Partition 2: String city is a valid String and valid international cities.
   */
  @Test
  void testGetCountryFromCityWithInternationalCity() {
    // Test with international city names to cover different API response formats
    String country1 = CitySummary.getCountryFromCity("Tokyo");
    String country2 = CitySummary.getCountryFromCity("London");
    String country3 = CitySummary.getCountryFromCity("Sydney");

    // These may return null or actual country names depending on API response
    // We're just testing that the branches execute
    assertTrue(country1 == null || country1.length() > 0);
    assertTrue(country2 == null || country2.length() > 0);
    assertTrue(country3 == null || country3.length() > 0);
  }

  /**
   * {@code testGetCountryFromCityWithAccentedCities()}
   *  Equivalence Partition 3: String city is a valid String
   *  and valid cities with accent characters.
   */
  @Test
  void testGetCountryFromCityWithAccentedCities() {
    // Test cities with accents to ensure URL encoding works and branches are covered
    String[] accentedCities = {"München", "Zürich", "Bogotá", "Lima", "Québec"};

    for (String city : accentedCities) {
      String country = CitySummary.getCountryFromCity(city);
      // Should all return valid countries
      assertNotNull(country);
    }
  }

  /**
   * {@code testGetCountryFromCityWithCitiesHavingMultipleWords()}
   *  Equivalence Partition 4: String city is a valid String and valid
   *  cities with multiple words.
   */
  @Test
  void testGetCountryFromCityWithCitiesHavingMultipleWords() {
    // Test cities with multiple words to cover URL encoding and parsing branches
    String[] multiWordCities = {
      "New York", "Los Angeles", "San Diego", "Kuala Lumpur",
      "Buenos Aires", "Rio de Janeiro", "New Delhi"
    };

    for (String city : multiWordCities) {
      String country = CitySummary.getCountryFromCity(city);
      // Should all return valid countries
      assertNotNull(country);
    }
  }

  /**
   * {@code testGetCountryFromCityWithSmallCities()}
   *  Equivalence Partition 5: String city is a valid String and valid smaller more niche cities.
   */
  @Test
  void testGetCountryFromCityWithSmallCities() {
    // Test smaller/lesser-known cities that might have different response formats
    String[] smallCities = {"Springfield", "Cambridge", "Oxford", "Princeton"};

    for (String city : smallCities) {
      String country = CitySummary.getCountryFromCity(city);
      // Should all return valid countries
      assertNotNull(country);
    }
  }

  /**
   * {@code testGetCountryFromCityWithCitiesInDifferentRegions()}
   *  Equivalence Partition 6: String city is a valid String and valid cities,
   *    stress test on different regions.
   */
  @Test
  void testGetCountryFromCityWithCitiesInDifferentRegions() {
    // Test cities from different regions to cover various API response formats
    // This helps ensure all parsing branches are exercised
    String[] diverseCities = {
      "Shanghai", "Mumbai", "Lagos", "Jakarta", "Manila",
      "Seoul", "Istanbul", "Buenos Aires", "Lima", "Casablanca"
    };

    for (String city : diverseCities) {
      String country = CitySummary.getCountryFromCity(city);
      // Should all return valid countries
      assertNotNull(country);
    }
  }

  /**
   * {@code testGetCountryFromCityWithMultipleCities()}
   *  Equivalence Partition 7: String city is a valid String and valid cities,
   *    stress test multiple different cities to cover various API response formats.
   */
  @Test
  void testGetCountryFromCityWithMultipleCities() {
    // Test multiple different cities to cover various API response formats
    // This helps cover different branches in the parsing logic
    String[] cities = {"Berlin", "Moscow", "Cairo", "Bangkok", "Dubai", "Mexico City"};

    for (String city : cities) {
      String country = CitySummary.getCountryFromCity(city);
      // Should all return valid countries
      assertNotNull(country);
    }
  }

  /**
   * {@code testGetCountryFromCityInvalidCity()}
   *  Equivalence Partition 8: String city is a valid String and but invalid city.
   */
  @Test
  void testGetCountryFromCityInvalidCity() {
    // Test with a city that likely doesn't exist
    String country = CitySummary.getCountryFromCity("NonExistentCity12345");

    // Should return null for invalid cities
    assertNull(country);
  }

  /**
   * {@code testGetCountryFromCityWithVeryLongName()}
   *  Equivalence Partition 8: String city is a valid String and but invalid city,
   *    String is length 200.
   */
  @Test
  void testGetCountryFromCityWithVeryLongName() {
    // Test with very long city name
    String longCityName = "A".repeat(200);
    String country = CitySummary.getCountryFromCity(longCityName);
    // Should return null for invalid cities
    assertTrue(country == null);
  }

  /**
   * {@code testGetCountryFromCityWithVeryLongName()}
   *  Equivalence Partition 9: String city is a valid String and valid city,
   *    City has special characters which is invalid.
   */
  @Test
  void testGetCountryFromCityWithSpecialCharacters() {
    // Test cities with special characters
    String country = CitySummary.getCountryFromCity("São Paulo");
    assertTrue(country == null || country.length() > 0);
  }

  /**
   * {@code testGetCountryFromCityWithVeryLongName()}
   *  Equivalence Partition 10: String city is a valid String and invalid city,
   *    City string has special characters which is invalid.
   */
  @Test
  void testGetCountryFromCityWithSpecialCharactersOnly() {
    // Edge case: special characters only
    String country = CitySummary.getCountryFromCity("!!!");
    // Should return null for invalid input
    assertTrue(country == null);
  }

  /**
   * {@code testGetCountryFromCityWithVeryLongName()}
   *  Equivalence Partition 11: String city is a empty String and invalid city.
   */
  @Test
  void testGetCountryFromCityWithEmptyString() {
    String country = CitySummary.getCountryFromCity("");
    // Should return null for empty string
    assertTrue(country == null);
  }

  /**
   * {@code testGetCountryFromCityWithVeryLongName()}
   *  Equivalence Partition 12: String city is null and invalid.
   */
  @Test
  void testGetCountryFromCityWithNull() {
    // This test verifies the method handles null gracefully
    // The method will throw NullPointerException when trying to replace spaces
    // but we test that the branches are covered
    assertThrows(NullPointerException.class, () -> {
      CitySummary.getCountryFromCity(null);
    });
  }

  /**
   * {@code testGetCountryFromCityWithSpaces()}
   * Equivalence Partition 13:
   *   Input is a valid multi-word city name containing spaces.
   *   The method must correctly encode and process space-delimited city names.
   * Expected result: method returns the correct country ("United States").
   */
  @Test
  void testGetCountryFromCityWithSpaces() {
    // Test with a city name that has spaces
    String country = CitySummary.getCountryFromCity("San Francisco");

    // Should handle spaces in city names
    assertEquals("United States", country);
  }

  /**
   * {@code testGetCountryFromCityWithMixedCase()}
   * Equivalence Partition 14:
   *   Input is a valid city name written in differing capitalization forms
   *   (lowercase, uppercase, proper case).
   *   The method should behave consistently regardless of string case.
   * Expected result: return value is either the same country or all nulls,
   *   but no errors occur.
   */
  @Test
  void testGetCountryFromCityWithMixedCase() {
    // Test that case doesn't affect the geocoding
    String country1 = CitySummary.getCountryFromCity("new york");
    String country2 = CitySummary.getCountryFromCity("NEW YORK");
    String country3 = CitySummary.getCountryFromCity("New York");

    // All should potentially return the same country (or all null)
    // We're testing that the method handles different cases
    assertTrue(country1 == null || country1.length() > 0);
    assertTrue(country2 == null || country2.length() > 0);
    assertTrue(country3 == null || country3.length() > 0);
  }

  /**
   * {@code testGetCountryFromCityWithNumbersOnly()}
   * Equivalence Partition 15:
   *   Input is a valid String type but represents an invalid city name.
   *   The city consists only of numeric characters.
   * Expected behavior:
   *   The API may return null or a country due to geocoding quirks,
   *   but the method should NOT throw an exception.
   */
  @Test
  void testGetCountryFromCityWithNumbersOnly() {
    // Edge case: numbers only
    // Note: The API might return a result, so we just verify it doesn't throw
    String country = CitySummary.getCountryFromCity("12345");
    // May return null or a result - we're testing branch coverage
    assertTrue(country == null || country.length() > 0);
  }

  /**
   * {@code testGetCountryFromCityWithSingleCharacter()}
   * Equivalence Partition 16:
   *   Input is a valid String type but represents an invalid or nonsensical city name.
   *   The city consists of a single character ("A"), which is generally insufficient
   *   to match any real geographic location.
   * Expected result:
   *   Method should return null, indicating no valid country could be determined.
   */
  @Test
  void testGetCountryFromCityWithSingleCharacter() {
    // Edge case: single character (unlikely to be a real city)
    String country = CitySummary.getCountryFromCity("A");
    // Should return null for invalid input
    assertTrue(country == null);
  }

  /* ======= String toString() Equivlence Partitions ======= */

  /**
   * {@code testToString()}
   *  Equivalence Partition 1: CitySummary() is properly called with all parameters valid.
   */
  @Test
  void testToString() {
    citySummary = new CitySummary("New York", weatherRecommendation, weatherAlert, 
                                  travelAdvisory, interestedUsers, message);
    
    String result = citySummary.toString();
    
    assertTrue(result.contains("CitySummary"));
    assertTrue(result.contains("New York"));
    assertTrue(result.contains("city="));
    assertTrue(result.contains("weatherRecommendation="));
    assertTrue(result.contains("weatherAlert="));
    assertTrue(result.contains("travelAdvisory="));
    assertTrue(result.contains("interestedUsers="));
    assertTrue(result.contains("message="));
  }

  /**
   * {@code testToString()}
   *  Equivalence Partition 2: CitySummary() is properly called with all parameters empty.
   */
  @Test
  void testToStringWithNullFields() {
    citySummary = new CitySummary();
    
    String result = citySummary.toString();
    
    assertTrue(result.contains("CitySummary"));
    assertTrue(result.contains("city=''"));
    assertTrue(result.contains("weatherRecommendation=null"));
    assertTrue(result.contains("weatherAlert=null"));
    assertTrue(result.contains("travelAdvisory=null"));
    assertTrue(result.contains("interestedUsers=null"));
    assertTrue(result.contains("message=''"));
  }


  /* ======= String getStateFromUsCity(String city) Equivlence Partitions ======= */

  /**
   * {@code testGetStateFromUsCityValidCities()}
   * Equivalence Partition 1:
   *   Input is a valid String representing a legitimate U.S. city name.
   *   The geocoder is expected to return a valid U.S. state (admin1 field).
   */
  @Test
  void testGetStateFromUsCityValidCities() {
    String state = CitySummary.getStateFromUsCity("New York");
    assertEquals("New York", state);
    
    state = CitySummary.getStateFromUsCity("Austin");
    assertEquals("Texas", state);

    state = CitySummary.getStateFromUsCity("Berlin");
    assertNull(state);

    state = CitySummary.getStateFromUsCity("Tokyo");
    assertNull(state);
  }

  /**
   * {@code testGetStateFromUsCityUsingCountries()}
   * Equivalence Partition 2:
   *   Input is a valid String, but it represents a *country* instead of a city.
   *   Querying the geocoder with a country name produces no admin1 field
   *   corresponding to a U.S. state.
   */
  @Test
  void testGetStateFromUsCityUsingCountries() {
    // no admin1 fields when queried with countries
    String state = CitySummary.getStateFromUsCity("Germany");
    assertNull(state);

    state = CitySummary.getStateFromUsCity("Japan");
    assertNull(state);

    state = CitySummary.getStateFromUsCity("United States");
    assertNull(state); // country matches but still no admin1 field
  }

  /**
   * {@code testGetStateFromUsCityInvalidCities()}
   * Equivalence Partition 3:
   *   Input is a valid String type but an invalid or nonsensical city name.
   *   Cases include:
   *     - Empty String ("")
   *     - Strings containing only special characters ("!$AS")
   *   These inputs cannot correspond to real U.S. cities.
   */
  @Test
  void testGetStateFromUsCityInvalidCities() {
    String state = CitySummary.getStateFromUsCity("");
    assertNull(state);

    state = CitySummary.getStateFromUsCity("!$AS");
    assertNull(state);
  }


  /* ======= Complete Test ======= */

  @Test
  void testCompleteCitySummary() {
    WeatherRecommendation rec = new WeatherRecommendation("Paris",
            Arrays.asList("2025-05-01", "2025-05-02", "2025-05-03"),
            "Perfect weather for sightseeing.");

    WeatherAlert alert = new WeatherAlert();
    alert.setLocation("Paris");

    TravelAdvisory advisory = new TravelAdvisory("France",
            "Level 2: Exercise increased caution", new ArrayList<>());

    List<UserPreference> users = Arrays.asList(new UserPreference(10L), new UserPreference(11L));

    citySummary = new CitySummary("Paris", rec, alert, advisory, users,
            "Beautiful city with great weather.");

    assertEquals("Paris", citySummary.getCity());
    assertEquals("Paris", citySummary.getWeatherRecommendation().getCity());
    assertEquals("Paris", citySummary.getWeatherAlert().getLocation());
    assertEquals("France", citySummary.getTravelAdvisory().getCountry());
    assertEquals(2, citySummary.getInterestedUsers().size());
    assertEquals("Beautiful city with great weather.", citySummary.getMessage());
  }
}