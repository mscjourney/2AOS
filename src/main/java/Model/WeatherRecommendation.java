package Model;

import java.util.List;

public class WeatherRecommendation {

  private String city;
  private List<String> recommendedDays;
  private String message;

  public WeatherRecommendation(String city, List<String> recommendedDays, String message) {
    this.city = city;
    this.recommendedDays = recommendedDays;
    this.message = message;
  }

  public WeatherRecommendation() {}

  // Getters and Setters
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public List<String> getRecommendedDays() {
    return recommendedDays;
  }

  public void setRecommendedDays(List<String> recommendedDays) {
    this.recommendedDays = recommendedDays;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  // toString() for debugging
  @Override
  public String toString() {
    return "WeatherRecommendation{" +
            "city='" + city + '\'' +
            ", recommendedDays=" + recommendedDays +
            ", message='" + message + '\'' +
            '}';
  }
}
