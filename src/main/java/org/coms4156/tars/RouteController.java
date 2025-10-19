package org.coms4156.tars;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** */
@RestController
public class RouteController {

  private final TarsService tarsService;

  public RouteController(TarsService tarsService) {
    this.tarsService = tarsService;
  }

  @GetMapping({"/", "/index"})
  public String index() {
    return "Welcome to the TARS Home Page!";
  }

  @PutMapping({"/user/{id}/add"})
  public ResponseEntity<?> addUser(@PathVariable int id, @RequestBody User user) {
    if (tarsService.addUser(user)) {
      return new ResponseEntity<>(user, HttpStatus.OK);
    }
    return new ResponseEntity<>("User Id already exists.", HttpStatus.CONFLICT);

  }

  @GetMapping({"/user/{id}"})
  public ResponseEntity<?> getUser(@PathVariable int id) {
    for (User user : tarsService.getUsers()) {
      if (user.getId() == id) {
        return new ResponseEntity<>(user, HttpStatus.OK);
      }
    }
    return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
  }

}