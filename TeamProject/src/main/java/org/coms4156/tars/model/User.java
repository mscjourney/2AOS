package org.coms4156.tars.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the User Preference model.
 */
public class User {
  private final int id;
  private final int clientId;
  private List<String> weatherPreferences;
  private List<String> temperaturePreferences;
  private List<String> cityPreferences;
  
  /**
   * Basic User constructor.
   *
   * @param id the id of the user
   * @param clientId the id of the client in charge of this user
   */
  public User(int id, int clientId) {
    if (id < 0) {
      throw new IllegalArgumentException("User Id cannot be negative.");
    }
    this.id = id;
    this.clientId = clientId;
    this.weatherPreferences = new ArrayList<>();
    this.temperaturePreferences = new ArrayList<>();
    this.cityPreferences = new ArrayList<>();
  }

  /**
   * Complete User constructor.
   *
   * @param clientId the id of the client in charge of this user
   * @param weatherPrefs a list containing the user's weather preferences
   * @param temperaturePrefs a list containing the user's temperature preferences
   * @param cityPrefs a list containing the user's city preferences
   */
  public User(int id, int clientId, List<String> weatherPrefs, 
                List<String> temperaturePrefs, List<String> cityPrefs) {
    if (id < 0) {
      throw new IllegalArgumentException("User Id cannot be negative.");
    }
    this.id = id;
    this.clientId = clientId;
    this.weatherPreferences = weatherPrefs;
    this.temperaturePreferences = temperaturePrefs;
    this.cityPreferences = cityPrefs;
  }

  /**
   * No args constructor.
   */
  public User() {
    this.id = 0;
    this.clientId = -1;
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

  public int getClientId() {
    return this.clientId;
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