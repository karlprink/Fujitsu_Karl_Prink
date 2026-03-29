package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.repository.CityStationMappingRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CityStationService.
 * Verifies the business logic for managing city-to-station mappings and
 * ensures that adding a new city correctly triggers an immediate weather data import.
 */
@ExtendWith(MockitoExtension.class)
class CityStationServiceTest {

    @Mock private CityStationMappingRepository repository;
    @Mock private WeatherImportService weatherImportService;

    @InjectMocks private CityStationService cityStationService;

    private CityStationMapping mockMapping;

    @BeforeEach
    void setUp() {
        mockMapping = CityStationMapping.builder()
                .id(1L)
                .city("KURESSAARE")
                .stationName("Roomassaare")
                .build();
    }

    /**
     * Tests that a station name is correctly returned when a valid city name is provided.
     */
    @Test
    void getStationNameByCity_existingCity_returnsStationName() {
        when(repository.findByCityIgnoreCase("Kuressaare")).thenReturn(Optional.of(mockMapping));

        Optional<String> result = cityStationService.getStationNameByCity("Kuressaare");

        assertTrue(result.isPresent());
        assertEquals("Roomassaare", result.get());
    }

    /**
     * Tests that an empty Optional is returned when searching for a city
     * that has no configured station mapping.
     */
    @Test
    void getStationNameByCity_nonExistingCity_returnsEmptyOptional() {
        when(repository.findByCityIgnoreCase("UNKNOWN")).thenReturn(Optional.empty());

        Optional<String> result = cityStationService.getStationNameByCity("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    /**
     * Tests that all configured mappings are retrieved correctly from the database.
     */
    @Test
    void getAllMappings_returnsListOfMappings() {
        when(repository.findAll()).thenReturn(List.of(mockMapping));

        List<CityStationMapping> result = cityStationService.getAllMappings();

        assertEquals(1, result.size());
        assertEquals("KURESSAARE", result.get(0).getCity());
    }

    /**
     * Tests the creation of a new city-station mapping.
     * Verifies that the city name is normalized to uppercase and that
     * the weather import process is triggered immediately after saving.
     */
    @Test
    void addCityStationMapping_savesToDatabase_andTriggersWeatherImport() {
        CityStationMapping inputMapping = CityStationMapping.builder()
                .city("kuressaare") // Testing lowercase input normalization
                .stationName("Roomassaare")
                .build();

        when(repository.save(any(CityStationMapping.class))).thenReturn(mockMapping);

        CityStationMapping result = cityStationService.addCityStationMapping(inputMapping);

        assertEquals("KURESSAARE", result.getCity());
        assertEquals("Roomassaare", result.getStationName());

        verify(weatherImportService, times(1)).importWeatherData();
    }
}