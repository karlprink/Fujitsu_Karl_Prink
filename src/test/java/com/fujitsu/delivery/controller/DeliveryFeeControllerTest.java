package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web MVC tests for the DeliveryFeeController.
 * Verifies REST endpoint accessibility, HTTP status codes, and JSON response mapping.
 */
@WebMvcTest(DeliveryFeeController.class)
class DeliveryFeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryFeeCalculationService feeCalculationService;

    /**
     * Tests a successful delivery fee calculation request.
     * Expects a 200 OK status and a JSON response with the correct fee amount.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void calculateFee_validRequest_returnsOkAndFee() throws Exception {

        when(feeCalculationService.calculateDeliveryFee("Tallinn", "Car"))
                .thenReturn(new DeliveryFeeDTO(4.0));

        mockMvc.perform(get("/api/delivery-fee")
                        .param("city", "Tallinn")
                        .param("vehicleType", "Car"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFee").value(4.0));
    }

    /**
     * Tests the scenario where weather conditions forbid the selected vehicle.
     * Expects the GlobalExceptionHandler to catch it and return a 400 Bad Request.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void calculateFee_forbiddenVehicle_returnsBadRequest() throws Exception {
        when(feeCalculationService.calculateDeliveryFee("Tallinn", "Bike"))
                .thenThrow(new VehicleForbiddenException("Usage of selected vehicle type is forbidden"));

        mockMvc.perform(get("/api/delivery-fee")
                        .param("city", "Tallinn")
                        .param("vehicleType", "Bike"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Usage of selected vehicle type is forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * Tests the scenario where a required request parameter is missing.
     * Expects a 400 Bad Request status generated automatically by Spring.
     *
     * @throws Exception if the MockMvc request execution fails
     */
    @Test
    void calculateFee_missingParameter_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/delivery-fee")
                        .param("city", "Tartu"))
                .andExpect(status().isBadRequest());
    }
}