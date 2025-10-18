package org.coms4156.tars;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RouteController {
  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }


  /**
   * Create new client route
   * @param clientId
   *
   * @return
   */
  @PostMapping("/api/v1/clients/{clientId}")
  public ResponseEntity<String> createClientRoute(@PathVariable String clientId) {
    // Logic to create a client would go here.
    return new ResponseEntity<>("Client route created for clientId: " + clientId, HttpStatus.CREATED);
  }
  
  /**
   * Handles POST requests to create a user.
   * @param clientId The ID of the client.
   * @param userId The ID of the user.
   *
   * @return A ResponseEntity indicating the result of the operation.
  */
  @PostMapping("/api/v1/clients/{clientId}/users/{userId}")
  public ResponseEntity<String> createClientUserRoute(@PathVariable String userId) {
    // Logic to create a user would go here.
    return new ResponseEntity<>("User route created for userId: " + userId, HttpStatus.CREATED);
  }
}