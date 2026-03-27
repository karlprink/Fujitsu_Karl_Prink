package com.fujitsu.delivery.repository;

import com.fujitsu.delivery.entity.WeatherData;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for managing WeatherData entities in database. */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

  /**
   * Finds the latest weather data for a specific station.
   *
   * @param stationName The name of the station (e.g., "Tallinn-Harku")
   * @return Optional containing the latest WeatherData if found
   */
  Optional<WeatherData> findFirstByStationNameOrderByObservationTimestampDesc(String stationName);

  boolean existsByStationNameAndObservationTimestamp(String stationName, LocalDateTime timestamp);

  Optional<WeatherData> findFirstByOrderByObservationTimestampDesc();
}
