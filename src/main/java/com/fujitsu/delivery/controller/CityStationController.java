package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.entity.CityStationMapping;
import com.fujitsu.delivery.service.CityStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityStationController {

    private final CityStationService cityStationService;

    @GetMapping
    public ResponseEntity<List<CityStationMapping>> getAllCities() {
        return ResponseEntity.ok(cityStationService.getAllMappings());
    }

    @PostMapping
    public ResponseEntity<CityStationMapping> addCity(@RequestBody CityStationMapping mapping) {
        return ResponseEntity.ok(cityStationService.addCityStationMapping(mapping));
    }
}