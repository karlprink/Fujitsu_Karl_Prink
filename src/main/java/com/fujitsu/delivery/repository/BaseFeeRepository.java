package com.fujitsu.delivery.repository;

import com.fujitsu.delivery.entity.BaseFee;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseFeeRepository extends JpaRepository<BaseFee, Long> {
  Optional<BaseFee> findByCityIgnoreCaseAndVehicleTypeIgnoreCase(String city, String vehicleType);
}
