package com.fujitsu.delivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when weather conditions forbid the usage of a specific vehicle type.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VehicleForbiddenException extends RuntimeException {
    public VehicleForbiddenException(String message) {

        super(message);
    }
}
