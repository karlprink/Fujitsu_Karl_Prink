package com.fujitsu.delivery.service;

import com.fujitsu.delivery.client.NationalWeatherServiceRestClient;
import com.fujitsu.delivery.dto.NationalWeatherServiceWeatherData;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.CityStationMappingRepository;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for importing weather data from external sources,
 * filtering relevant stations, and managing data persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WeatherImportService {

  private final WeatherDataRepository weatherDataRepository;
  private final NationalWeatherServiceRestClient weatherClient;
  private final CityStationMappingRepository cityStationMappingRepository;

  @Value("${weather.import.cron}")
  private String cronExpressionString;

  /**
   * Fetches new weather data, filters for active stations, and saves to the database.
   * Uses @Transactional to ensure atomicity; if an error occurs during saving,
   * the transaction is rolled back.
   */
  public void importWeatherData() {
    try {
      NationalWeatherServiceWeatherData.Observations observations = weatherClient.fetchObservations();

      // Early-return
      if (observations == null || observations.getStations() == null) {
        log.warn("Weather data XML was empty or malformed. Aborting import.");
        return;
      }

      LocalDateTime observationTime = LocalDateTime.ofInstant(
              Instant.ofEpochSecond(observations.getTimestamp()), ZoneId.systemDefault());

      // Dynamically retrieve all stations currently being tracked (case-insensitive)
      Set<String> activeStations = cityStationMappingRepository.findAll().stream()
              .map(mapping -> mapping.getStationName().toUpperCase())
              .collect(Collectors.toSet());

      List<WeatherData> weatherDataList = observations.getStations().stream()
              .filter(station -> activeStations.contains(station.getName().toUpperCase()))
              // Ensure we don't save duplicate observations for the same timestamp
              .filter(station -> !weatherDataRepository.existsByStationNameAndObservationTimestamp(
                      station.getName(), observationTime))
              .map(station -> buildWeatherData(station, observationTime))
              .toList();

      saveImportedData(weatherDataList);

    } catch (Exception e) {
      log.error("Failed to import weather data", e);
      throw new RuntimeException("Weather data import failed", e);
    }
  }

  /**
   * Persists the filtered list of weather data to the database.
   * * @param weatherDataList List of entities to be saved
   */
  private void saveImportedData(List<WeatherData> weatherDataList) {
    if (!weatherDataList.isEmpty()) {
      weatherDataRepository.saveAll(weatherDataList);
      log.info("Successfully imported and saved {} NEW weather data records.", weatherDataList.size());
    } else {
      log.info("No new weather data to import. Database is already up to date.");
    }
  }

  /**
   * Logic executed on startup to determine if an immediate import is required.
   * Uses flattened logic instead of deeply nested blocks.
   */
  public void checkAndImportDataOnStartup() {
    log.info("Checking if initial weather data import is needed...");
    var latestData = weatherDataRepository.findFirstByOrderByObservationTimestampDesc();

    // Import if DB is empty or if the existing data is considered stale
    if (latestData.isEmpty() || isDataStale(latestData.get().getObservationTimestamp())) {
      log.info("Data is missing or stale. Triggering startup import.");
      importWeatherData();
    } else {
      log.info("Recent weather data is fresh enough. Skipping startup import.");
    }
  }

  /**
   * Checks if the last saved data is older than the configured cron interval.
   * * @param latestTimestamp Timestamp of the most recent database record
   * @return true if data is stale, false otherwise
   */
  private boolean isDataStale(LocalDateTime latestTimestamp) {
    Duration cronInterval = calculateCronInterval(cronExpressionString);
    // If cron cannot be parsed, default to importing for safety
    if (cronInterval == null) return true;

    LocalDateTime now = LocalDateTime.now();
    return latestTimestamp.isBefore(now.minus(cronInterval));
  }

  /**
   * Calculates the duration between scheduled executions based on the cron expression.
   * * @param cronStr The cron expression string
   * @return Duration between intervals or null if parsing fails
   */
  private Duration calculateCronInterval(String cronStr) {
    try {
      CronExpression cron = CronExpression.parse(cronStr);
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime nextExecution = cron.next(now);
      if (nextExecution != null) {
        LocalDateTime afterNextExecution = cron.next(nextExecution);
        if (afterNextExecution != null) {
          return Duration.between(nextExecution, afterNextExecution);
        }
      }
    } catch (Exception e) {
      log.warn("Could not parse cron expression for dynamic interval calculation.", e);
    }
    return null;
  }

  /**
   * Maps a station DTO from the XML source to a WeatherData entity.
   * * @param station The DTO from the XML
   * @param timestamp The observation timestamp
   * @return A mapped WeatherData entity
   */
  private WeatherData buildWeatherData(NationalWeatherServiceWeatherData.Station station, LocalDateTime timestamp) {
    return WeatherData.builder()
            .stationName(station.getName())
            .wmoCode(station.getWmocode())
            .airTemperature(station.getAirTemperature())
            .windSpeed(station.getWindSpeed())
            .weatherPhenomenon(station.getWeatherPhenomenon())
            .observationTimestamp(timestamp)
            .build();
  }
}