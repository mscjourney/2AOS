package org.coms4156.tars.dto;

import java.util.List;

/**
 * Data Transfer Object for UserPreference responses.
 */
public class UserPreferenceDto {
  private Long id;
  private List<String> weatherPreferences;
  private List<String> temperaturePreferences;
  private List<String> cityPreferences;

  public UserPreferenceDto() { }

  public UserPreferenceDto(Long id, List<String> weatherPreferences,
                           List<String> temperaturePreferences, List<String> cityPreferences) {
    this.id = id;
    this.weatherPreferences = weatherPreferences;
    this.temperaturePreferences = temperaturePreferences;
    this.cityPreferences = cityPreferences;
  }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public List<String> getWeatherPreferences() { return weatherPreferences; }
  public void setWeatherPreferences(List<String> weatherPreferences) { this.weatherPreferences = weatherPreferences; }
  public List<String> getTemperaturePreferences() { return temperaturePreferences; }
  public void setTemperaturePreferences(List<String> temperaturePreferences) { this.temperaturePreferences = temperaturePreferences; }
  public List<String> getCityPreferences() { return cityPreferences; }
  public void setCityPreferences(List<String> cityPreferences) { this.cityPreferences = cityPreferences; }
}
