package com.fujitsu.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "city_station_mapping")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityStationMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String city;

  @Column(nullable = false)
  private String stationName;
}
