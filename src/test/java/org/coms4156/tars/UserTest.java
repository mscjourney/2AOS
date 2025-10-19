package org.coms4156.tars;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;


@SpringBootTest
@AutoConfigureMockMvc
public class UserTest {
    
  @Autowired
  private MockMvc mockMvc;

  @Test
  public void getUserTest() throws Exception {
    this.mockMvc.perform(get("/user/2")).andDo(print())
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id", is(2)))
      .andExpect(jsonPath("$.weatherPreferences", contains("rainy")))
      .andExpect(jsonPath("$.temperaturePreferences", contains("60F", "67F")))
      .andExpect(jsonPath("$.cityPreferences", contains("New York", "Paris")));

    this.mockMvc.perform(get("/user/0")).andDo(print())
      .andExpect(content().string(containsString("User not found.")));
  }
}