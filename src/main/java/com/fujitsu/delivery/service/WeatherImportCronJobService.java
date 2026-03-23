package com.fujitsu.delivery.service;

import com.fujitsu.delivery.dto.WeatherDataXmlDto;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherImportCronJobService {
    private final RestTemplate restTemplate;
    private final WeatherDataRepository weatherDataRepository;

    @Value("${weather.import.url}")
    private String weatherUrl;

    private static final Set<String> TARGET_STATIONS = Set.of(
            "Tallinn-Harku",
            "Tartu-Tõravere",
            "Pärnu"
    );

    /**
     * Scheduled task to fetch weather data from the external portal, filter required stations,
     * and save them to the database to keep a permanent history.
     *
     * @throws RuntimeException if the HTTP request fails or XML parsing encounters an error
     */
    @Scheduled(cron = "${weather.import.cron}")
    public void importWeatherData() {
        log.info("Starting scheduled weather data import from URL: {}", weatherUrl);

        try {
            WeatherDataXmlDto.Observations observations = restTemplate.getForObject(weatherUrl, WeatherDataXmlDto.Observations.class);

            if (observations != null && observations.getStations() != null) {
                LocalDateTime observationTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(observations.getTimestamp()),
                        ZoneId.systemDefault()
                );

                List<WeatherData> weatherDataList = observations.getStations().stream()
                        .filter(station -> TARGET_STATIONS.contains(station.getName()))
                        .filter(station -> !weatherDataRepository.existsByStationNameAndObservationTimestamp(station.getName(), observationTime))
                        .map(station -> buildWeatherData(station, observationTime))
                        .toList();

                if (!weatherDataList.isEmpty()) {
                    weatherDataRepository.saveAll(weatherDataList);
                    log.info("Successfully imported and saved {} NEW weather data records.", weatherDataList.size());
                } else {
                    log.info("No new weather data to import. Database is already up to date.");
                }
            } else {
                log.warn("Weather data XML was empty or malformed.");
            }

        } catch (Exception e) {
            log.error("Failed to import weather data", e);
            throw new RuntimeException("Weather data import failed", e);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void importDataOnStartup() {
        log.info("Application started. Forcing an initial weather data import...");
        importWeatherData();
    }

    /**
     * Maps a parsed XML station DTO to the database Entity.
     *
     * @param station   The parsed station data
     * @param timestamp The observation timestamp extracted from the XML root
     * @return WeatherData entity ready to be saved
     */
    private WeatherData buildWeatherData(WeatherDataXmlDto.Station station, LocalDateTime timestamp) {
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