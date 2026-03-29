package com.fujitsu.delivery.service;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.repository.CityStationMappingRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Service class for managing the relationship between delivery cities and weather observation
 * stations. This service provides methods for dynamic station lookups and real-time mapping
 * updates.
 */
@Service
@RequiredArgsConstructor
public class CityStationService {

  private final CityStationMappingRepository repository;

  private final WeatherImportService weatherImportService;

  /**
   * Resolves the weather station name for a given city. Results are cached to optimize performance
   * and reduce database load during high-frequency delivery fee calculations.
   *
   * @param city The name of the city to lookup (case-insensitive).
   * @return An {@link Optional} containing the station name if found, otherwise an empty Optional.
   */
  @Cacheable("stationMappings")
  public Optional<String> getStationNameByCity(String city) {
    return repository.findByCityIgnoreCase(city).map(CityStationMapping::getStationName);
  }

  /**
   * Retrieves all existing city-to-station mappings from the database.
   *
   * @return A list of all {@link CityStationMapping} entities.
   */
  public List<CityStationMapping> getAllMappings() {
    return repository.findAll();
  }

  /**
   * Registers a new city-to-station mapping. This method normalizes the city name to uppercase,
   * persists it, and evicts the existing mapping cache to ensure data consistency. Additionally, it
   * triggers an immediate weather data import for the newly added configuration.
   *
   * @param mapping The mapping entity containing city and station details.
   * @return The saved {@link CityStationMapping} entity.
   */
  @CacheEvict(value = "stationMappings", allEntries = true)
  public CityStationMapping addCityStationMapping(CityStationMapping mapping) {
    mapping.setCity(mapping.getCity().toUpperCase());
    CityStationMapping savedMapping = repository.save(mapping);
    weatherImportService.importWeatherData();

    return savedMapping;
  }
}
