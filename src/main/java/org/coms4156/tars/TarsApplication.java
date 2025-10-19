package org.coms4156.tars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Start up for TARS Application.
 */
@SpringBootApplication
public class TarsApplication {
  public static void main(String[] args) {
    // localhost:8080
    SpringApplication.run(TarsApplication.class, args);
  }
}
