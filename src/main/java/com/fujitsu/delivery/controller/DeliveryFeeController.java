package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.dto.DeliveryFeeResponse;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for calculating delivery fees. This controller serves as the primary entry point
 * for the delivery fee calculation engine, integrating regional base fees with real-time or
 * historical weather surcharges.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/delivery-fee", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
    name = "1. Delivery Fee Calculator",
    description =
        "Endpoints for calculating the final delivery fee based on vehicle, city, and weather.")
public class DeliveryFeeController {

  private final DeliveryFeeCalculationService deliveryFeeCalculationService;

  /**
   * Calculates the delivery fee based on the specified city and vehicle type. The calculation
   * incorporates regional base fees and weather conditions. If a timestamp is provided, the fee is
   * calculated based on weather data closest to that time.
   *
   * @param city The target city for delivery (e.g., Tallinn, Tartu, Pärnu).
   * @param vehicleType The type of vehicle used (e.g., Car, Scooter, Bike).
   * @param timestamp Optional ISO-8601 timestamp for historical fee calculation.
   * @return A {@link ResponseEntity} containing the {@link DeliveryFeeResponse} with the total fee.
   * @throws IllegalArgumentException if the vehicle type is forbidden due to weather or parameters
   *     are invalid.
   */
  @GetMapping
  @Operation(
      summary = "Calculate delivery fee",
      description =
          "Calculates the total delivery fee for a specific city and vehicle type. Automatically applies weather-based surcharges if applicable.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Fee calculated successfully"),
        @ApiResponse(
            responseCode = "400",
            description =
                "Invalid input, missing parameters, or vehicle usage is forbidden by weather conditions",
            content = @Content),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or weather data unavailable",
            content = @Content)
      })
  public ResponseEntity<DeliveryFeeResponse> calculateFee(
      @Parameter(
              description = "City where the delivery takes place",
              example = "Tallinn",
              required = true)
          @RequestParam
          String city,
      @Parameter(
              description = "Type of the vehicle used for delivery",
              example = "Bike",
              required = true)
          @RequestParam
          String vehicleType,
      @Parameter(
              description =
                  "Optional timestamp for historical fee calculation (ISO format: yyyy-MM-ddTHH:mm:ss)",
              example = "2026-03-27T12:00:00",
              required = false)
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime timestamp) {
    DeliveryFeeResponse response =
        deliveryFeeCalculationService.calculateDeliveryFee(city, vehicleType, timestamp);
    return ResponseEntity.ok(response);
  }
}
