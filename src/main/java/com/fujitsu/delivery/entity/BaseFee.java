package com.fujitsu.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistence entity representing a regional base delivery fee. This entity defines the starting
 * price for a specific combination of city and vehicle type before any weather-related surcharges
 * are applied.
 */
@Entity
@Table(name = "base_fees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseFee {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String vehicleType;

  @Column(nullable = false)
  private Double fee;
}
