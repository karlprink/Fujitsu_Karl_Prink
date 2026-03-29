package com.fujitsu.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistence entity representing the dynamic mapping between a delivery city and an official
 * weather observation station. This entity allows the system to resolve which station's weather
 * data should be used when calculating fees for a specific city.
 */
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
