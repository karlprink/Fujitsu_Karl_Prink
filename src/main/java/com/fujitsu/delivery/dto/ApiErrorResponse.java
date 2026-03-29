package com.fujitsu.delivery.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object representing a standardized error response for the API. This structure
 * ensures that all exceptions caught by the Global Exception Handler return a consistent JSON
 * format to the client.
 */
@Data
@Builder
public class ApiErrorResponse {
  private LocalDateTime timestamp;
  private int status;
  private String error;
  private String message;
}
