package com.fujitsu.delivery.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web MVC tests for the DeliveryFeeController. Verifies REST endpoint accessibility, HTTP status
 * codes, and JSON response mapping.
 */
@WebMvcTest(DeliveryFeeController.class)
class DeliveryFeeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DeliveryFeeCalculationService feeCalculationService;

  /**
   * Tests a successful delivery fee calculation request. Expects a 200 OK status and a JSON
   * response with the correct fee amount.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_validRequest_returnsOkAndFee() throws Exception {

    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Car"))
        .thenReturn(new DeliveryFeeDTO(4.0));

    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tallinn").param("vehicleType", "Car"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFee").value(4.0));
  }

  /**
   * Tests the scenario where weather conditions forbid the selected vehicle. Expects the
   * GlobalExceptionHandler to catch it and return a 400 Bad Request.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_forbiddenVehicle_returnsBadRequest() throws Exception {
    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Bike"))
        .thenThrow(new VehicleForbiddenException("Usage of selected vehicle type is forbidden"));

    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tallinn").param("vehicleType", "Bike"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Usage of selected vehicle type is forbidden"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  /**
   * Tests the scenario where a required request parameter is missing. Expects a 400 Bad Request
   * status generated automatically by Spring.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_missingParameter_returnsBadRequest() throws Exception {
    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tartu"))
        .andExpect(status().isBadRequest());
  }

  /**
   * Tests the scenario where an invalid argument is provided (e.g., an unknown city). Verifies that
   * the GlobalExceptionHandler intercepts IllegalArgumentException and translates it into a 400 Bad
   * Request JSON response.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_invalidArgument_returnsBadRequest() throws Exception {
    // Arrange: throw an IllegalArgumentException
    when(feeCalculationService.calculateDeliveryFee("Atlantis", "Car"))
        .thenThrow(new IllegalArgumentException("Invalid city: Atlantis"));

    // Act & Assert: 400 Bad Request formatting
    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Atlantis").param("vehicleType", "Car"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid city: Atlantis"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  /**
   * Tests the catch-all exception handler for unexpected internal server errors. Verifies that the
   * GlobalExceptionHandler intercepts generic Exceptions and returns a safe 500 Internal Server
   * Error JSON response without leaking sensitive stack traces.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_unexpectedError_returnsInternalServerError() throws Exception {
    // Arrange: RuntimeException
    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Car"))
        .thenThrow(new RuntimeException("Database connection completely failed"));

    // Act & Assert: 500 Internal Server Error formatting
    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tallinn").param("vehicleType", "Car"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred"))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
