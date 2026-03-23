package com.fujitsu.delivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler to provide consistent JSON error responses across the REST API.
 * This class intercepts exceptions thrown by controllers and services and formats them
 * into a standardized client-friendly response.
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
    public ResponseEntity<Object> handleVehicleForbiddenException(VehicleForbiddenException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Handles exceptions thrown due to invalid input parameters, such as an unsupported city,
     * invalid vehicle type, or missing weather data for a calculation.
     *
     * @param exception The caught IllegalArgumentException
     * @return ResponseEntity containing a 400 Bad Request status and the error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * Handles Spring exceptions thrown when a required API request parameter
     * (e.g., 'city' or 'vehicleType') is missing from the request URL.
     *
     * @param ex The caught MissingServletRequestParameterException containing the missing parameter name
     * @return ResponseEntity containing a 400 Bad Request status and a descriptive error message
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = "Missing required parameter: " + ex.getParameterName();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Catch-all handler for unexpected internal server errors.
     * Logs the actual error for the developer and returns a generic 500 error to the client,
     * preventing sensitive stack traces from being exposed.
     *
     * @param exception The caught unexpected Exception
     * @return ResponseEntity containing a 500 Internal Server Error status and a generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralException(Exception exception) {
        System.err.println("Unexpected internal error " + exception.getMessage());
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred");
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
