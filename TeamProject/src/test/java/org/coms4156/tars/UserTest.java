package org.coms4156.tars;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.coms4156.tars.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

// import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains the unit tests for the User related functionalities.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserTest { 
  // Currnently directly modifies the data/userPreferences.json
  // Need to create a dummy version for testing so that we don't have to revert each time.
    
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void indexTest() throws Exception {
    this.mockMvc.perform(get("/")).andDo(print())
      .andExpect(status().isOk())
      .andExpect(content().string(containsString("Welcome to the TARS Home Page!")));
  }

  @Test
  public void getUserTest() throws Exception {
    this.mockMvc.perform(get("/user/2")).andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(2)))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("60F", "67F")))
      .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));

    this.mockMvc.perform(get("/user/0")).andDo(print())
      .andExpect(status().isNotFound())
      .andExpect(content().string(containsString("User not found.")));
  }

  @Test
  public void addUserTest() throws Exception {
    List<String> weatherPreferences = new ArrayList<>();
    weatherPreferences.add("snowy");

    List<String> temperaturePreferences = new ArrayList<>();
    temperaturePreferences.add("88F");
    temperaturePreferences.add("15C");

    List<String> cityPreferences = new ArrayList<>();
    cityPreferences.add("Rome");
    cityPreferences.add("Syndey");
    
    ObjectMapper mapper = new ObjectMapper();

    User newUser = new User(3, weatherPreferences, temperaturePreferences, cityPreferences);

    this.mockMvc.perform(put("/user/3/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(3)))
        .andExpect(jsonPath("$.weatherPreferences", contains("snowy")))
        .andExpect(jsonPath("$.temperaturePreferences", contains("88F", "15C")))
        .andExpect(jsonPath("$.cityPreferences", contains("Rome", "Syndey")));

    this.mockMvc.perform(put("/user/3/add")
      .contentType("application/json")
      .content(mapper.writeValueAsString(newUser)))
        .andDo(print())
        .andExpect(status().isConflict())
        .andExpect(content().string(containsString("User Id already exists.")));
  }
}