package com.fujitsu.delivery.repository;

import com.fujitsu.delivery.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing WeatherData entities in database.
 */

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
}