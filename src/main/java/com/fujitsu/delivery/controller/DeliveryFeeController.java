package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.dto.DeliveryFeeResponse;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/delivery-fee", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "1. Delivery Fee Calculator", description = "Endpoints for calculating the final delivery fee based on vehicle, city, and weather.")
public class DeliveryFeeController {

    private final DeliveryFeeCalculationService deliveryFeeCalculationService;

    /**
     * Calculates the delivery fee based on the specified city and vehicle type. The calculation
     * incorporates regional base fees and current weather conditions.
     *
     * @param city        The target city for delivery (Tallinn, Tartu, Pärnu)
     * @param vehicleType The type of vehicle used (Car, Scooter, Bike)
     * @return ResponseEntity containing the calculated total delivery fee
     */
    @GetMapping
    @Operation(
            summary = "Calculate delivery fee",
            description = "Calculates the total delivery fee for a specific city and vehicle type. Automatically applies weather-based surcharges if applicable."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fee calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input, missing parameters, or vehicle usage is forbidden by weather conditions", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error or weather data unavailable", content = @Content)
    })
    public ResponseEntity<DeliveryFeeResponse> calculateFee(
            @Parameter(description = "City where the delivery takes place", example = "Tallinn", required = true)
            @RequestParam String city,
            @Parameter(description = "Type of the vehicle used for delivery", example = "Bike", required = true)
            @RequestParam String vehicleType,
            @Parameter(
                    description = "Optional timestamp for historical fee calculation (ISO format: yyyy-MM-ddTHH:mm:ss)",
                    example = "2026-03-27T12:00:00",
                    required = false
            )
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime timestamp) {
        DeliveryFeeResponse response =
                deliveryFeeCalculationService.calculateDeliveryFee(city, vehicleType, timestamp);
        return ResponseEntity.ok(response);
    }
}
