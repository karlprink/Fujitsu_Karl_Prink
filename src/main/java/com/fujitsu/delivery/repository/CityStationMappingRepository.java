package com.fujitsu.delivery.repository;

import com.fujitsu.delivery.entity.CityStationMapping;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityStationMappingRepository extends JpaRepository<CityStationMapping, Long> {
  Optional<CityStationMapping> findByCityIgnoreCase(String city);
}
