package com.fujitsu.delivery.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.service.BaseFeeService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC tests for the FeeRuleController. Verifies the REST API endpoints for managing BaseFee
 * rules (CRUD operations).
 */
@WebMvcTest(FeeRuleController.class)
class FeeRuleControllerTest {

  private static final String BASE_URL = "/api/rules/base-fees";
  private static final String TALLINN = "TALLINN";
  private static final String TARTU = "TARTU";
  private static final String NARVA = "NARVA";
  private static final String BIKE = "BIKE";
  private static final String CAR = "CAR";
  private static final String SCOOTER = "SCOOTER";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BaseFeeService baseFeeService;

  /**
   * Tests fetching all base fees. Expects a 200 OK status and a JSON array containing the rules.
   */
  @Test
  void getAllBaseFees_returnsListOfFees() throws Exception {
    BaseFee fee1 = BaseFee.builder().id(1L).city(TALLINN).vehicleType(BIKE).fee(3.0).build();
    BaseFee fee2 = BaseFee.builder().id(2L).city(TARTU).vehicleType(CAR).fee(3.5).build();

    when(baseFeeService.getAllBaseFees()).thenReturn(List.of(fee1, fee2));

    mockMvc
            .perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].city").value(TALLINN))
            .andExpect(jsonPath("$[1].city").value(TARTU));
  }

  /** Tests creating a new base fee. Expects a 200 OK status and the saved JSON object. */
  @Test
  void createBaseFee_validRequest_returnsSavedFee() throws Exception {
    BaseFee newFee = BaseFee.builder().city(NARVA).vehicleType(SCOOTER).fee(2.0).build();
    BaseFee savedFee = BaseFee.builder().id(3L).city(NARVA).vehicleType(SCOOTER).fee(2.0).build();

    when(baseFeeService.createBaseFee(any(BaseFee.class))).thenReturn(savedFee);

    mockMvc
            .perform(
                    post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newFee)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.city").value(NARVA));
  }

  /**
   * Tests updating an existing base fee successfully. Expects a 200 OK status and the updated JSON
   * object.
   */
  @Test
  void updateBaseFee_existingId_returnsUpdatedFee() throws Exception {
    Long id = 1L;
    BaseFee updateRequest = BaseFee.builder().city(TALLINN).vehicleType(BIKE).fee(4.0).build();
    BaseFee updatedFee = BaseFee.builder().id(id).city(TALLINN).vehicleType(BIKE).fee(4.0).build();

    when(baseFeeService.updateBaseFee(eq(id), any(BaseFee.class))).thenReturn(Optional.of(updatedFee));

    mockMvc
            .perform(
                    put(BASE_URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fee").value(4.0));
  }

  /** Tests updating a base fee that does not exist. Expects a 404 Not Found status. */
  @Test
  void updateBaseFee_nonExistingId_returnsNotFound() throws Exception {
    Long id = 99L;
    BaseFee updateRequest = BaseFee.builder().city(TALLINN).vehicleType(BIKE).fee(4.0).build();

    when(baseFeeService.updateBaseFee(eq(id), any(BaseFee.class))).thenReturn(Optional.empty());

    mockMvc
            .perform(
                    put(BASE_URL + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
  }

  /**
   * Tests deleting an existing base fee. Expects a 200 OK status and verifies the repository delete
   * method was called.
   */
  @Test
  void deleteBaseFee_existingId_returnsOk() throws Exception {
    Long id = 1L;
    when(baseFeeService.deleteBaseFee(id)).thenReturn(true);

    mockMvc.perform(delete(BASE_URL + "/{id}", id)).andExpect(status().isOk());

    verify(baseFeeService, times(1)).deleteBaseFee(id);
  }

  /**
   * Tests deleting a base fee that does not exist. Expects a 404 Not Found status and verifies the
   * repository delete method was never called.
   */
  @Test
  void deleteBaseFee_nonExistingId_returnsNotFound() throws Exception {
    Long id = 99L;
    when(baseFeeService.deleteBaseFee(id)).thenReturn(false);

    mockMvc.perform(delete(BASE_URL + "/{id}", id)).andExpect(status().isNotFound());

    verify(baseFeeService, times(1)).deleteBaseFee(id);
  }

  /**
   * Tests that sending a malformed JSON request body triggers the custom GlobalExceptionHandler and
   * returns a helpful error message with the expected format.
   */
  @Test
  void createBaseFee_malformedJson_returnsBadRequestWithExpectedFormat() throws Exception {
    String malformedJson =
            """
                    {
                        "city": "TALLINN"
                        "vehicleType": "BIKE",
                        "fee": 4.0
                    }""";

    mockMvc
            .perform(
                    post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(
                    jsonPath("$.message")
                            .value("Malformed JSON request. Please check your request body format."))
            .andExpect(jsonPath("$.expectedFormat").exists())
            .andExpect(jsonPath("$.expectedFormat.city").value("String (e.g., 'TALLINN')"));
  }
}