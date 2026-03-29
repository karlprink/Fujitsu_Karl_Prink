package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the BaseFeeService. This class verifies the core business logic for managing base
 * fee rules, including retrieval, creation with duplicate validation, updates, and deletion.
 */
@ExtendWith(MockitoExtension.class)
class BaseFeeServiceTest {

  @Mock private BaseFeeRepository baseFeeRepository;

  @InjectMocks private BaseFeeService baseFeeService;

  private BaseFee mockBaseFee;

  /** Initializes a common mock BaseFee object before each test execution. */
  @BeforeEach
  void setUp() {
    mockBaseFee = BaseFee.builder().id(1L).city("TALLINN").vehicleType("BIKE").fee(3.0).build();
  }

  /**
   * Tests that getAllBaseFees retrieves all records from the repository. Verifies that the
   * repository's findAll method is invoked exactly once.
   */
  @Test
  void getAllBaseFees_returnsListOfBaseFees() {
    when(baseFeeRepository.findAll()).thenReturn(List.of(mockBaseFee));

    List<BaseFee> result = baseFeeService.getAllBaseFees();

    assertEquals(1, result.size());
    assertEquals("TALLINN", result.getFirst().getCity());
    verify(baseFeeRepository, times(1)).findAll();
  }

  /**
   * Tests the successful creation of a new base fee rule. Verifies that the service checks for
   * existing duplicates and saves the new rule if none are found.
   */
  @Test
  void createBaseFee_validNewFee_savesAndReturnsFee() {
    BaseFee newFee = BaseFee.builder().city("TARTU").vehicleType("CAR").fee(3.5).build();

    // Simulate that no duplicate exists in the database
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TARTU", "CAR"))
        .thenReturn(Optional.empty());
    when(baseFeeRepository.save(newFee)).thenReturn(newFee);

    BaseFee savedFee = baseFeeService.createBaseFee(newFee);

    assertNotNull(savedFee);
    assertEquals("TARTU", savedFee.getCity());
    assertEquals(3.5, savedFee.getFee());
    verify(baseFeeRepository, times(1)).save(newFee);
  }

  /**
   * Tests that creating a base fee fails if a rule for the same city and vehicle type already
   * exists. Verifies that an IllegalArgumentException is thrown and the save method is never
   * called.
   */
  @Test
  void createBaseFee_duplicateFee_throwsIllegalArgumentException() {
    BaseFee duplicateFee = BaseFee.builder().city("TALLINN").vehicleType("BIKE").fee(4.0).build();

    // Simulate that a rule for the TALLINN + BIKE combination already exists
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "BIKE"))
        .thenReturn(Optional.of(mockBaseFee));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> baseFeeService.createBaseFee(duplicateFee));

    assertTrue(exception.getMessage().contains("already exists"));
    verify(baseFeeRepository, never()).save(any(BaseFee.class));
  }

  /**
   * Tests the update logic for an existing base fee rule. Verifies that fields are correctly mapped
   * from the request to the existing entity before saving.
   */
  @Test
  void updateBaseFee_existingId_updatesAndReturnsFee() {
    Long id = 1L;
    BaseFee updateRequest = BaseFee.builder().city("TALLINN").vehicleType("BIKE").fee(5.0).build();

    when(baseFeeRepository.findById(id)).thenReturn(Optional.of(mockBaseFee));
    when(baseFeeRepository.save(any(BaseFee.class))).thenReturn(mockBaseFee);

    Optional<BaseFee> updatedFee = baseFeeService.updateBaseFee(id, updateRequest);

    assertTrue(updatedFee.isPresent());
    assertEquals(5.0, updatedFee.get().getFee());
    verify(baseFeeRepository, times(1)).save(mockBaseFee);
  }

  /**
   * Tests updating a base fee rule that does not exist. Verifies that an empty Optional is returned
   * and no save operation is attempted.
   */
  @Test
  void updateBaseFee_nonExistingId_returnsEmptyOptional() {
    Long id = 99L;
    BaseFee updateRequest = BaseFee.builder().city("TALLINN").vehicleType("BIKE").fee(5.0).build();

    when(baseFeeRepository.findById(id)).thenReturn(Optional.empty());

    Optional<BaseFee> updatedFee = baseFeeService.updateBaseFee(id, updateRequest);

    assertTrue(updatedFee.isEmpty());
    verify(baseFeeRepository, never()).save(any(BaseFee.class));
  }

  /**
   * Tests the successful deletion of a base fee rule by its ID. Verifies that the service checks
   * for existence before calling the delete method.
   */
  @Test
  void deleteBaseFee_existingId_deletesAndReturnsTrue() {
    Long id = 1L;
    when(baseFeeRepository.existsById(id)).thenReturn(true);

    boolean isDeleted = baseFeeService.deleteBaseFee(id);

    assertTrue(isDeleted);
    verify(baseFeeRepository, times(1)).deleteById(id);
  }

  /**
   * Tests deleting a base fee rule that does not exist. Verifies that the service returns false and
   * does not attempt a delete operation.
   */
  @Test
  void deleteBaseFee_nonExistingId_returnsFalse() {
    Long id = 99L;
    when(baseFeeRepository.existsById(id)).thenReturn(false);

    boolean isDeleted = baseFeeService.deleteBaseFee(id);

    assertFalse(isDeleted);
    verify(baseFeeRepository, never()).deleteById(id);
  }
}
