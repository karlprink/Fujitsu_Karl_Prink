package com.fujitsu.delivery.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistence entity representing a historical weather observation record. This entity stores
 * snapshots of weather conditions fetched from the National Weather Service, which are then used to
 * calculate dynamic delivery surcharges.
 */
@Entity
@Table(name = "weather_data")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherData {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String stationName;
  private String wmoCode;
  private Double airTemperature;
  private Double windSpeed;
  private String weatherPhenomenon;
  private LocalDateTime observationTimestamp;
}
