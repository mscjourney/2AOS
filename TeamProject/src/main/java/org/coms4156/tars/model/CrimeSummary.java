import java.util.List;

public class CrimeSummary {
  private String state;
  private List<String> dates;
  private String message;

  public CrimeSummary(String state, List<String> dates, String message) {
    this.state = state;
    this.dates = dates;
    this.message = message;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public List<String> getDates() {
    return dates;
  }

  public void setDates(List<String> dates) {
    this.dates = dates;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "CrimeSummary {" +
            "state='" + state + '\'' +
            ", dates=" + dates +
            ", message='" + message + '\'' +
            '}';
  }
}
