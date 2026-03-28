package com.fujitsu.delivery.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler to provide consistent JSON error responses across the REST API. This
 * class intercepts exceptions thrown by controllers and services and formats them into a
 * standardized client-friendly response.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles exceptions thrown when weather conditions forbid the usage of a specific vehicle type.
   *
   * @param exception The caught VehicleForbiddenException
   * @return ResponseEntity containing a 400 Bad Request status and the error details
   */
  @ExceptionHandler(VehicleForbiddenException.class)
  public ResponseEntity<Object> handleVehicleForbiddenException(
      VehicleForbiddenException exception) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  /**
   * Handles exceptions thrown due to invalid input parameters, such as an unsupported city, invalid
   * vehicle type, or missing weather data for a calculation.
   *
   * @param exception The caught IllegalArgumentException
   * @return ResponseEntity containing a 400 Bad Request status and the error details
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  /**
   * Handles Spring exceptions thrown when a required API request parameter (e.g., 'city' or
   * 'vehicleType') is missing from the request URL.
   *
   * @param ex The caught MissingServletRequestParameterException containing the missing parameter
   *     name
   * @return ResponseEntity containing a 400 Bad Request status and a descriptive error message
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
    String message = "Missing required parameter: " + ex.getParameterName();
    return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
  }

  /**
   * Catch-all handler for unexpected internal server errors. Logs the actual error for the
   * developer and returns a generic 500 error to the client, preventing sensitive stack traces from
   * being exposed.
   *
   * @param exception The caught unexpected Exception
   * @return ResponseEntity containing a 500 Internal Server Error status and a generic message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleGeneralException(Exception exception) {
    System.err.println("Unexpected internal error " + exception.getMessage());
    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred");
  }

  /**
   * Handles HttpMessageNotReadableException, which is typically thrown when a client sends a
   * request with a malformed or unparseable JSON body. * Instead of returning a generic server
   * error, this handler constructs a user-friendly response containing a clear error message and an
   * example of the expected JSON format to help the API consumer correct their request.
   *
   * @return a ResponseEntity containing the error details and the expected format structure, with a
   *     400 Bad Request HTTP status
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Object> handleMalformedJsonException() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Bad Request");
    body.put("message", "Malformed JSON request. Please check your request body format.");

    Map<String, String> expectedFormat = new LinkedHashMap<>();
    expectedFormat.put("city", "String (e.g., 'TALLINN')");
    expectedFormat.put("vehicleType", "String (e.g., 'BIKE')");
    expectedFormat.put("fee", "Number (e.g., 3.5)");

    body.put("expectedFormat", expectedFormat);

    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);

    return new ResponseEntity<>(body, status);
  }
}
