package com.fujitsu.delivery.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fujitsu.delivery.dto.DeliveryFeeResponse;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import java.time.LocalDateTime;
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
class DeliveryFeeControllerTest { // TODO tee integratsioonitestid ilma servicei mockita

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DeliveryFeeCalculationService feeCalculationService;

  /**
   * Tests a successful delivery fee calculation request without a timestamp.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_validRequest_returnsOkAndFee() throws Exception { // TODO teadmiseks: given when then
    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Car", null))
        .thenReturn(new DeliveryFeeResponse(4.0));

    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tallinn").param("vehicleType", "Car"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFee").value(4.0));
  }

  /**
   * Tests a successful delivery fee calculation request WITH a historical timestamp.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_withTimestamp_returnsOkAndFee() throws Exception {
    LocalDateTime testTime = LocalDateTime.parse("2026-03-27T10:00:00");

    when(feeCalculationService.calculateDeliveryFee("Tartu", "Bike", testTime))
        .thenReturn(new DeliveryFeeResponse(3.5));

    mockMvc
        .perform(
            get("/api/delivery-fee")
                .param("city", "Tartu")
                .param("vehicleType", "Bike")
                .param("timestamp", "2026-03-27T10:00:00"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalFee").value(3.5));
  }

  /**
   * Tests the scenario where weather conditions forbid the selected vehicle.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_forbiddenVehicle_returnsBadRequest() throws Exception {
    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Bike", null))
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
   * Tests the scenario where a required request parameter is missing.
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
   * Tests the scenario where an invalid argument is provided.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_invalidArgument_returnsBadRequest() throws Exception {
    when(feeCalculationService.calculateDeliveryFee("Atlantis", "Car", null))
        .thenThrow(new IllegalArgumentException("Invalid city: Atlantis"));

    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Atlantis").param("vehicleType", "Car"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid city: Atlantis"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  /**
   * Tests the catch-all exception handler for unexpected internal server errors.
   *
   * @throws Exception if the MockMvc request execution fails
   */
  @Test
  void calculateFee_unexpectedError_returnsInternalServerError() throws Exception {
    when(feeCalculationService.calculateDeliveryFee("Tallinn", "Car", null))
        .thenThrow(new RuntimeException("Database connection completely failed"));

    mockMvc
        .perform(get("/api/delivery-fee").param("city", "Tallinn").param("vehicleType", "Car"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.error").value("Internal Server Error"))
        .andExpect(jsonPath("$.message").value("An unexpected internal server error occurred"))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
