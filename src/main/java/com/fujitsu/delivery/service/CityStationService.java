package com.fujitsu.delivery.service;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.repository.CityStationMappingRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CityStationService {

  private final CityStationMappingRepository repository;

  private final WeatherImportService weatherImportService;

  @Cacheable("stationMappings")
  public Optional<String> getStationNameByCity(String city) {
    return repository.findByCityIgnoreCase(city).map(CityStationMapping::getStationName);
  }

  public List<CityStationMapping> getAllMappings() {
    return repository.findAll();
  }

  @CacheEvict(value = "stationMappings", allEntries = true)
  public CityStationMapping addCityStationMapping(CityStationMapping mapping) {
    mapping.setCity(mapping.getCity().toUpperCase());
    CityStationMapping savedMapping = repository.save(mapping);
    weatherImportService.importWeatherData();

    return savedMapping;
  }
}
