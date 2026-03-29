package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.client.NationalWeatherServiceRestClient;
import com.fujitsu.delivery.dto.NationalWeatherServiceWeatherData;
import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.CityStationMappingRepository;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for WeatherImportService using Mockito.
 * Verifies the dynamic filtering, persistence logic, and startup synchronization.
 */
@ExtendWith(MockitoExtension.class)
class WeatherImportServiceTest {

  @Mock private WeatherDataRepository weatherDataRepository;
  @Mock private NationalWeatherServiceRestClient weatherClient;
  @Mock private CityStationMappingRepository cityStationMappingRepository;

  @Captor private ArgumentCaptor<List<WeatherData>> weatherDataListCaptor;

  /**
   * Spy is used to allow mocking specific internal method calls
   * while testing the startup logic.
   */
  private WeatherImportService weatherImportService;

  @BeforeEach
  void setUp() {
    weatherImportService = Mockito.spy(new WeatherImportService(
            weatherDataRepository,
            weatherClient,
            cityStationMappingRepository
    ));

    // Injecting the cron expression manually since @Value is not processed in unit tests
    ReflectionTestUtils.setField(
            weatherImportService, "cronExpressionString", "0 15 * * * ?");
  }

  /**
   * Tests that weather data is correctly fetched, dynamically filtered based on
   * active city mappings, and saved to the repository.
   */
  @Test
  void importWeatherData_success_filtersAndSavesCorrectStations() {
    // Arrange: Mock active city-to-station mappings from the database
    CityStationMapping tallinnMapping = CityStationMapping.builder().city("TALLINN").stationName("Tallinn-Harku").build();
    CityStationMapping tartuMapping = CityStationMapping.builder().city("TARTU").stationName("Tartu-Tõravere").build();
    when(cityStationMappingRepository.findAll()).thenReturn(List.of(tallinnMapping, tartuMapping));

    // Arrange: Mock the XML API response with three stations (one should be filtered out)
    NationalWeatherServiceWeatherData.Observations mockObservations = new NationalWeatherServiceWeatherData.Observations();
    mockObservations.setTimestamp(1710000000L); // Random Unix timestamp

    NationalWeatherServiceWeatherData.Station tallinn = createMockStation("Tallinn-Harku", "26038", 2.5, 4.1, "Light snow shower");
    NationalWeatherServiceWeatherData.Station tartu = createMockStation("Tartu-Tõravere", "26242", -1.5, 2.0, "");
    NationalWeatherServiceWeatherData.Station randomCity = createMockStation("Võru", "26258", 0.0, 1.0, ""); // Unmapped station

    mockObservations.setStations(List.of(tallinn, tartu, randomCity));

    when(weatherClient.fetchObservations()).thenReturn(mockObservations);
    when(weatherDataRepository.existsByStationNameAndObservationTimestamp(anyString(), any(LocalDateTime.class)))
            .thenReturn(false);

    // Act
    weatherImportService.importWeatherData();

    // Assert: Verify only mapped stations were saved
    verify(weatherDataRepository, times(1)).saveAll(weatherDataListCaptor.capture());
    List<WeatherData> savedData = weatherDataListCaptor.getValue();

    assertEquals(2, savedData.size());
    assertEquals("Tallinn-Harku", savedData.get(0).getStationName());
    assertEquals("Tartu-Tõravere", savedData.get(1).getStationName());
  }

  /**
   * Tests that the service does nothing when the API returns an empty or null response.
   */
  @Test
  void importWeatherData_emptyResponse_doesNothing() {
    // Arrange
    when(weatherClient.fetchObservations()).thenReturn(null);

    // Act
    weatherImportService.importWeatherData();

    // Assert
    verify(weatherDataRepository, never()).saveAll(any());
  }

  /**
   * Tests that a RuntimeException is thrown and logged when the external API call fails.
   */
  @Test
  void importWeatherData_apiError_throwsRuntimeException() {
    // Arrange
    when(weatherClient.fetchObservations()).thenThrow(new RuntimeException("API is down"));

    // Act & Assert
    RuntimeException exception = assertThrows(RuntimeException.class, () -> weatherImportService.importWeatherData());
    assertEquals("Weather data import failed", exception.getMessage());
    verify(weatherDataRepository, never()).saveAll(any());
  }

  /**
   * Tests that an import is triggered on application startup if the database is empty.
   */
  @Test
  void checkAndImportDataOnStartup_databaseEmpty_triggersImport() {
    // Arrange
    when(weatherDataRepository.findFirstByOrderByObservationTimestampDesc())
            .thenReturn(Optional.empty());

    // Prevent the actual data fetching call
    doNothing().when(weatherImportService).importWeatherData();

    // Act
    weatherImportService.checkAndImportDataOnStartup();

    // Assert
    verify(weatherImportService, times(1)).importWeatherData();
  }

  /**
   * Tests that startup import is skipped when recent data (within the cron interval)
   * already exists in the database.
   */
  @Test
  void checkAndImportDataOnStartup_recentDataExists_skipsImport() {
    // Arrange: Cron interval is 1 hour, mock data is only 30 minutes old
    WeatherData recentData = WeatherData.builder()
            .observationTimestamp(LocalDateTime.now().minusMinutes(30)).build();

    when(weatherDataRepository.findFirstByOrderByObservationTimestampDesc())
            .thenReturn(Optional.of(recentData));

    // Act
    weatherImportService.checkAndImportDataOnStartup();

    // Assert
    verify(weatherImportService, never()).importWeatherData();
  }

  /**
   * Tests that startup import is triggered when the latest data in the database
   * is older than the configured cron interval.
   */
  @Test
  void checkAndImportDataOnStartup_oldDataExists_triggersImport() {
    // Arrange: Cron interval is 1 hour, mock data is 65 minutes old
    WeatherData oldData = WeatherData.builder()
            .observationTimestamp(LocalDateTime.now().minusMinutes(65)).build();

    when(weatherDataRepository.findFirstByOrderByObservationTimestampDesc())
            .thenReturn(Optional.of(oldData));

    doNothing().when(weatherImportService).importWeatherData();

    // Act
    weatherImportService.checkAndImportDataOnStartup();

    // Assert
    verify(weatherImportService, times(1)).importWeatherData();
  }

  /**
   * Helper method to create mock station DTOs.
   * * @param name Station name
   * @param wmo WMO code
   * @param temp Air temperature
   * @param wind Wind speed
   * @param phenomenon Weather phenomenon
   * @return Mapped WeatherDataXmlDTO.Station object
   */
  private NationalWeatherServiceWeatherData.Station createMockStation(
          String name, String wmo, Double temp, Double wind, String phenomenon) {
    NationalWeatherServiceWeatherData.Station station = new NationalWeatherServiceWeatherData.Station();
    station.setName(name);
    station.setWmocode(wmo);
    station.setAirTemperature(temp);
    station.setWindSpeed(wind);
    station.setWeatherPhenomenon(phenomenon);
    return station;
  }
}