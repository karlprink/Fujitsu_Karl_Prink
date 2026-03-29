package com.fujitsu.delivery.controller;

import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.service.BaseFeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/rules/base-fees", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(
    name = "3. Base Fee Rules Management",
    description =
        "Endpoints for managing the core base fee matrix for different cities and vehicle types.")
public class FeeRuleController {

  private final BaseFeeService baseFeeService;

  @GetMapping
  @Operation(
      summary = "Get all base fee rules",
      description = "Retrieves the complete list of configured base fees.")
  public ResponseEntity<List<BaseFee>> getAllBaseFees() {
    return ResponseEntity.ok(baseFeeService.getAllBaseFees());
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Create a new base fee rule",
      description =
          "Adds a new base fee rule. Will fail if a rule for the given city and vehicle combination already exists.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Rule created successfully"),
    @ApiResponse(responseCode = "400", description = "Duplicate rule already exists")
  })
  public ResponseEntity<BaseFee> createBaseFee(@RequestBody BaseFee baseFee) {
    return ResponseEntity.ok(baseFeeService.createBaseFee(baseFee));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Update an existing base fee rule",
      description = "Updates the city, vehicle type, or fee amount for a specific rule ID.")
  public ResponseEntity<BaseFee> updateBaseFee(
      @Parameter(description = "ID of the base fee rule to update", required = true) @PathVariable
          Long id,
      @RequestBody BaseFee updatedFee) {
    return baseFeeService
        .updateBaseFee(id, updatedFee)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a base fee rule",
      description = "Removes a specific base fee rule by its ID.")
  public ResponseEntity<Void> deleteBaseFee(
      @Parameter(description = "ID of the base fee rule to delete", required = true) @PathVariable
          Long id) {
    if (baseFeeService.deleteBaseFee(id)) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }
}
