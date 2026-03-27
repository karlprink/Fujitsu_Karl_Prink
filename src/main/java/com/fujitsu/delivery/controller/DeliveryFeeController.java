package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.service.DeliveryFeeCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/delivery-fee")
@RequiredArgsConstructor
public class DeliveryFeeController {

  private final DeliveryFeeCalculationService deliveryFeeCalculationService;

  /**
   * Calculates the delivery fee based on the specified city and vehicle type. The calculation
   * incorporates regional base fees and current weather conditions.
   *
   * @param city The target city for delivery (Tallinn, Tartu, Pärnu)
   * @param vehicleType The type of vehicle used (Car, Scooter, Bike)
   * @return ResponseEntity containing the calculated total delivery fee
   */
  @GetMapping
  public ResponseEntity<DeliveryFeeDTO> calculateFee(
      @RequestParam String city, @RequestParam String vehicleType) {
    DeliveryFeeDTO response = deliveryFeeCalculationService.calculateDeliveryFee(city, vehicleType);
    return ResponseEntity.ok(response);
  }
}
