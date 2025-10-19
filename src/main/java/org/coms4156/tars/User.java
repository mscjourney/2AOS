package org.coms4156.tars;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the User Preference model.
 */
public class User {
  private final int id;
  private List<String> weatherPreferences;
  private List<String> temperaturePreferences;
  private List<String> cityPreferences;
  
  /**
   * Basic User constructor.
   *
   * @param id the id of the user
   */
  public User(int id) {
    this.id = id;
    this.weatherPreferences = new ArrayList<>();
    this.temperaturePreferences = new ArrayList<>();
    this.cityPreferences = new ArrayList<>();
  }

  /**
   * Complete User constructor.
   *
   * @param weatherPrefs a list containing the user's weather preferences
   * @param temperaturePrefs a list containing the user's temperature preferences
   * @param cityPrefs a list containing the user's city preferences
   */
  public User(int id, List<String> weatherPrefs, 
                List<String> temperaturePrefs, List<String> cityPrefs) {
    this.id = id;
    this.weatherPreferences = weatherPrefs;
    this.temperaturePreferences = temperaturePrefs;
    this.cityPreferences = cityPrefs;
  }

  /**
   * No args constructor.
   */
  public User() {
    this.id = 0;
    this.weatherPreferences = new ArrayList<>();
    this.temperaturePreferences = new ArrayList<>();
    this.cityPreferences = new ArrayList<>();
  }

  /**
   * Sets the weather preferences of a user.
   *
   * @param weatherPrefs a list containing the user's weather preferences
   */
  public void setWeatherPreferences(List<String> weatherPrefs) {
    if (weatherPrefs == null) {
      this.weatherPreferences = new ArrayList<>();
    } else {
      this.weatherPreferences = weatherPrefs;
    }
  }

  public List<String> getWeatherPreferences() {
    return this.weatherPreferences;
  }

  /**
   * Sets the temperature preferences of a user.
   *
   * @param temperaturePrefs a list containing the user's temperature preferences 
   */
  public void setTemperaturePreferences(List<String> temperaturePrefs) {
    if (temperaturePrefs == null) {
      this.temperaturePreferences = new ArrayList<>();
    } else {
      this.temperaturePreferences = temperaturePrefs;
    }
  }

  public List<String> getTemperaturePreferences() {
    return this.temperaturePreferences;
  }

  /**
   * Sets the city preferences of a user.
   *
   * @param cityPrefs a list containing the user's city preferences
   */
  public void setCityPreferences(List<String> cityPrefs) {
    if (cityPrefs == null) {
      this.cityPreferences = new ArrayList<>();
    } else {
      this.cityPreferences = cityPrefs;
    }
  }

  public List<String> getCityPreferences() {
    return this.cityPreferences;
  }

  public int getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    User user = (User) obj;
    return this.id == user.id;
  }

  @Override
  public int hashCode() {
    return this.id;
  }
}