package com.fujitsu.delivery.repository;

import com.fujitsu.delivery.entity.CityStationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CityStationMappingRepository extends JpaRepository<CityStationMapping, Long> {
    Optional<CityStationMapping> findByCityIgnoreCase(String city);
}