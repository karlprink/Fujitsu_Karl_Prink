package com.fujitsu.delivery.service;

import com.fujitsu.delivery.dto.WeatherDataXmlDto;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WeatherImportCronService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class WeatherImportCronServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @InjectMocks
    private WeatherImportCronJobService weatherImportCronJobService;

    @Captor
    private ArgumentCaptor<List<WeatherData>> weatherDataListCaptor;

    @BeforeEach
    void setUp() {
        // Süstime URL-i, kuna @Value annotatsioon standardse Unit Testi puhul automaatselt ei tööta
        ReflectionTestUtils.setField(weatherImportCronJobService, "weatherUrl", "http://dummy-url.com");
    }

    /**
     * Tests that weather data is correctly fetched, filtered, and saved.
     */
    @Test
    void importWeatherData_success_filtersAndSavesCorrectStations() {
        // 1. Arrange (Seadista testandmed)
        WeatherDataXmlDto.Observations mockObservations = new WeatherDataXmlDto.Observations();
        mockObservations.setTimestamp(1710000000L); // Suvaline Unix timestamp

        WeatherDataXmlDto.Station tallinn = createMockStation("Tallinn-Harku", "26038", 2.5, 4.1, "Light snow shower");
        WeatherDataXmlDto.Station tartu = createMockStation("Tartu-Tõravere", "26242", -1.5, 2.0, "");
        WeatherDataXmlDto.Station randomCity = createMockStation("Võru", "26258", 0.0, 1.0, ""); // Seda ei tohiks salvestada!

        mockObservations.setStations(List.of(tallinn, tartu, randomCity));

        when(restTemplate.getForObject(anyString(), eq(WeatherDataXmlDto.Observations.class)))
                .thenReturn(mockObservations);

        // 2. Act (Käivita meetod)
        weatherImportCronJobService.importWeatherData();

        // 3. Assert (Kontrolli tulemust)
        verify(weatherDataRepository, times(1)).saveAll(weatherDataListCaptor.capture());

        List<WeatherData> savedData = weatherDataListCaptor.getValue();

        // Kontrollime, et salvestati täpselt 2 jaama (Tallinn ja Tartu), Võru filtreeriti välja
        assertEquals(2, savedData.size());
        assertEquals("Tallinn-Harku", savedData.get(0).getStationName());
        assertEquals("Tartu-Tõravere", savedData.get(1).getStationName());
    }

    /**
     * Tests that no data is saved when the API response is empty.
     */
    @Test
    void importWeatherData_emptyResponse_doesNothing() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherDataXmlDto.Observations.class)))
                .thenReturn(null);

        // Act
        weatherImportCronJobService.importWeatherData();

        // Assert
        verify(weatherDataRepository, never()).saveAll(any());
    }

    /**
     * Tests that a RuntimeException is thrown when the API call fails.
     */
    @Test
    void importWeatherData_apiError_throwsRuntimeException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(WeatherDataXmlDto.Observations.class)))
                .thenThrow(new RuntimeException("API is down"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            weatherImportCronJobService.importWeatherData();
        });

        assertEquals("Weather data import failed", exception.getMessage());
        verify(weatherDataRepository, never()).saveAll(any());
    }

    // test data
    private WeatherDataXmlDto.Station createMockStation(String name, String wmo, Double temp, Double wind, String phenomenon) {
        WeatherDataXmlDto.Station station = new WeatherDataXmlDto.Station();
        station.setName(name);
        station.setWmo(wmo);
        station.setAirTemperature(temp);
        station.setWindSpeed(wind);
        station.setWeatherPhenomenon(phenomenon);
        return station;
    }
}