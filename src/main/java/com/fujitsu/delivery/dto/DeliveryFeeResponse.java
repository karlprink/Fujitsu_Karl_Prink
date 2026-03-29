package com.fujitsu.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for providing the result of a delivery fee calculation. This object is
 * returned to the client upon a successful GET request to the delivery fee calculation endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryFeeResponse {
  private Double totalFee;
}
