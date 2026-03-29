package com.fujitsu.delivery.client;

import com.fujitsu.delivery.dto.WeatherDataXmlDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * REST client for interacting with the Estonian National Weather Service API.
 * Handles the low-level HTTP communication to fetch raw weather data.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NationalWeatherServiceRestClient {

    private final RestTemplate restTemplate;

    @Value("${weather.import.url}")
    private String weatherUrl;

    /**
     * Fetches the latest weather observations from the external XML source.
     * * @return WeatherDataXmlDTO.Observations containing the timestamp and list of stations
     * @throws org.springframework.web.client.RestClientException if the API request fails
     */
    public WeatherDataXmlDTO.Observations fetchObservations() {
        log.info("Fetching weather data from external portal: {}", weatherUrl);
        return restTemplate.getForObject(weatherUrl, WeatherDataXmlDTO.Observations.class);
    }
}