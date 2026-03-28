package com.fujitsu.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
