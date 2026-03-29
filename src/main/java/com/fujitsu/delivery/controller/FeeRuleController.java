package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.dto.ApiErrorResponse;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.service.BaseFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rules/base-fees")
@RequiredArgsConstructor
public class FeeRuleController {

  private final BaseFeeService baseFeeService;

  @GetMapping
  public ResponseEntity<List<BaseFee>> getAllBaseFees() {
    return ResponseEntity.ok(baseFeeService.getAllBaseFees());
  }

  @PostMapping
  public ResponseEntity<BaseFee> createBaseFee(@RequestBody BaseFee baseFee) {
    return ResponseEntity.ok(baseFeeService.createBaseFee(baseFee));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BaseFee> updateBaseFee(@PathVariable Long id, @RequestBody BaseFee updatedFee) {
    return baseFeeService.updateBaseFee(id, updatedFee)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBaseFee(@PathVariable Long id) {
    if (baseFeeService.deleteBaseFee(id)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message) {
    ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .build();

    return new ResponseEntity<>(errorResponse, status);
  }
}