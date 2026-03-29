package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.service.CityStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing the association between delivery cities and weather observation
 * stations. This allows for dynamic expansion of supported regions without code modifications.
 */
@RestController
@RequestMapping(value = "/api/cities", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(
    name = "2. City & Station Management",
    description = "Endpoints for dynamically mapping cities to National Weather Service stations.")
public class CityStationController {

  private final CityStationService cityStationService;

  /**
   * Retrieves a comprehensive list of all city-to-station mappings currently stored in the system.
   *
   * @return A {@link ResponseEntity} containing a list of {@link CityStationMapping} objects.
   */
  @GetMapping
  @Operation(
      summary = "Get all city mappings",
      description =
          "Retrieves a list of all currently configured cities and their corresponding weather stations.")
  public ResponseEntity<List<CityStationMapping>> getAllCities() {
    return ResponseEntity.ok(cityStationService.getAllMappings());
  }

  /**
   * Creates a new mapping between a city and a weather station. Successfully adding a mapping
   * automatically triggers an immediate weather data import for the newly registered station.
   *
   * @param mapping The city and station mapping details provided in the request body.
   * @return A {@link ResponseEntity} containing the saved {@link CityStationMapping}.
   */
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Add a new city mapping",
      description =
          "Maps a new city to a weather station. Automatically triggers an immediate weather data import for the new configuration.")
  @ApiResponse(
      responseCode = "200",
      description = "City mapping added successfully and weather data imported")
  public ResponseEntity<CityStationMapping> addCity(@RequestBody CityStationMapping mapping) {
    return ResponseEntity.ok(cityStationService.addCityStationMapping(mapping));
  }
}
