package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.service.CityStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@Tag(name = "2. City & Station Management", description = "Endpoints for dynamically mapping cities to National Weather Service stations.")
public class CityStationController {

    private final CityStationService cityStationService;

    @GetMapping
    @Operation(summary = "Get all city mappings", description = "Retrieves a list of all currently configured cities and their corresponding weather stations.")
    public ResponseEntity<List<CityStationMapping>> getAllCities() {
        return ResponseEntity.ok(cityStationService.getAllMappings());
    }

    @PostMapping
    @Operation(
            summary = "Add a new city mapping",
            description = "Maps a new city to a weather station. Automatically triggers an immediate weather data import for the new configuration."
    )
    @ApiResponse(responseCode = "200", description = "City mapping added successfully and weather data imported")
    public ResponseEntity<CityStationMapping> addCity(@RequestBody CityStationMapping mapping) {
        return ResponseEntity.ok(cityStationService.addCityStationMapping(mapping));
    }
}