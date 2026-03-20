package com.fujitsu.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for responding with the calculated delivery fee.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryFeeDTO {
    private Double totalFee;
}