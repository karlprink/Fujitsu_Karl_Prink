package com.fujitsu.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.service.CityStationService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC tests for the CityStationController. Verifies the REST API endpoints for viewing and
 * adding dynamic city-station mappings using MockMvc to simulate HTTP requests.
 */
@WebMvcTest(CityStationController.class)
class CityStationControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CityStationService cityStationService;

  /**
   * Tests the GET /api/cities endpoint. Expects a JSON array containing all configured
   * city-to-station mappings.
   */
  @Test
  void getAllCities_returnsListOfMappings() throws Exception {
    CityStationMapping mapping =
        CityStationMapping.builder().id(1L).city("TALLINN").stationName("Tallinn-Harku").build();

    when(cityStationService.getAllMappings()).thenReturn(List.of(mapping));

    mockMvc
        .perform(get("/api/cities"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$[0].city").value("TALLINN"))
        .andExpect(jsonPath("$[0].stationName").value("Tallinn-Harku"));
  }

  /**
   * Tests the POST /api/cities endpoint. Expects a successful 200 OK response with the saved
   * mapping object in JSON format.
   */
  @Test
  void addCity_validRequest_returnsSavedMapping() throws Exception {
    CityStationMapping newMapping =
        CityStationMapping.builder().city("KURESSAARE").stationName("Roomassaare").build();

    CityStationMapping savedMapping =
        CityStationMapping.builder().id(2L).city("KURESSAARE").stationName("Roomassaare").build();

    when(cityStationService.addCityStationMapping(any(CityStationMapping.class)))
        .thenReturn(savedMapping);

    mockMvc
        .perform(
            post("/api/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMapping)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.city").value("KURESSAARE"))
        .andExpect(jsonPath("$.stationName").value("Roomassaare"));
  }
}
