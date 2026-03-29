package com.fujitsu.delivery.adapter;

import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WeatherDataAdapter {

    private final WeatherDataRepository weatherDataRepository;

    public WeatherData getLatestWeatherData(String stationName, LocalDateTime targetTime) {
        return weatherDataRepository
                .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                        stationName, targetTime)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Weather data not found for station: " + stationName + " at " + targetTime));
    }
}