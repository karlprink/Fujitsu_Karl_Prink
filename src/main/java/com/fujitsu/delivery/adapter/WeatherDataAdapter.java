package com.fujitsu.delivery.adapter;

import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeatherDataAdapter {

  private final WeatherDataRepository weatherDataRepository;

  /**
   * Retrieves the most recent weather data for a specific station that occurred at or before the
   * given target time.
   *
   * @param stationName The name of the weather station (e.g., "Tallinn-Harku").
   * @param targetTime The reference timestamp; the method looks for the closest record that is less
   *     than or equal to this time.
   * @return The latest available {@link WeatherData} for the specified criteria.
   * @throws IllegalArgumentException If no weather data is found for the given station at or before
   *     the specified time.
   */
  public WeatherData getLatestWeatherData(String stationName, LocalDateTime targetTime) {
    return weatherDataRepository
        .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
            stationName, targetTime)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Weather data not found for station: " + stationName + " at " + targetTime));
  }
}
