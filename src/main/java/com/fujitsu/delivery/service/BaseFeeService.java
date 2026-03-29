package com.fujitsu.delivery.service;

import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing the delivery base fee rules. Handles the creation, retrieval,
 * updating, and deletion of base fees, ensuring that duplicate rules for the same city and vehicle
 * type are not created.
 */
@Service
@RequiredArgsConstructor
public class BaseFeeService {

  private final BaseFeeRepository baseFeeRepository;

  /**
   * Retrieves all configured base fee rules from the database.
   *
   * @return a list of all BaseFee entities
   */
  public List<BaseFee> getAllBaseFees() {
    return baseFeeRepository.findAll();
  }

  /**
   * Creates a new base fee rule. Validates that a rule for the specified city and vehicle type
   * combination does not already exist.
   *
   * @param baseFee the BaseFee entity to be saved
   * @return the saved BaseFee entity
   * @throws IllegalArgumentException if a rule for the given city and vehicle type already exists
   */
  public BaseFee createBaseFee(BaseFee baseFee) {
    Optional<BaseFee> existingFee =
        baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase(
            baseFee.getCity(), baseFee.getVehicleType());

    if (existingFee.isPresent()) {
      throw new IllegalArgumentException(
          String.format(
              "Base fee rule for city '%s' and vehicle '%s' already exists.",
              baseFee.getCity(), baseFee.getVehicleType()));
    }

    return baseFeeRepository.save(baseFee);
  }

  /**
   * Updates an existing base fee rule identified by its ID.
   *
   * @param id the ID of the base fee rule to update
   * @param updatedFee the new data for the base fee rule
   * @return an Optional containing the updated BaseFee if found and saved, or an empty Optional if
   *     not found
   */
  public Optional<BaseFee> updateBaseFee(Long id, BaseFee updatedFee) {
    return baseFeeRepository
        .findById(id)
        .map(
            existingFee -> {
              existingFee.setCity(updatedFee.getCity());
              existingFee.setVehicleType(updatedFee.getVehicleType());
              existingFee.setFee(updatedFee.getFee());
              return baseFeeRepository.save(existingFee);
            });
  }

  /**
   * Deletes a base fee rule by its ID.
   *
   * @param id the ID of the base fee rule to delete
   * @return true if the rule existed and was deleted, false if the rule was not found
   */
  public boolean deleteBaseFee(Long id) {
    if (baseFeeRepository.existsById(id)) {
      baseFeeRepository.deleteById(id);
      return true;
    }
    return false;
  }
}
