package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules/base-fees")
@RequiredArgsConstructor
public class FeeRuleController {

  private final BaseFeeRepository baseFeeRepository;

  @GetMapping
  public ResponseEntity<List<BaseFee>> getAllBaseFees() {
    return ResponseEntity.ok(baseFeeRepository.findAll());
  }

  @PostMapping
  public ResponseEntity<BaseFee> createBaseFee(@RequestBody BaseFee baseFee) {
    return ResponseEntity.ok(baseFeeRepository.save(baseFee));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseFee> updateBaseFee(
      @PathVariable Long id, @RequestBody BaseFee updatedFee) {
    return baseFeeRepository
        .findById(id)
        .map(
            existingFee -> {
              existingFee.setCity(updatedFee.getCity());
              existingFee.setVehicleType(updatedFee.getVehicleType());
              existingFee.setFee(updatedFee.getFee());
              return ResponseEntity.ok(baseFeeRepository.save(existingFee));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBaseFee(@PathVariable Long id) {
    if (baseFeeRepository.existsById(id)) {
      baseFeeRepository.deleteById(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }
}
