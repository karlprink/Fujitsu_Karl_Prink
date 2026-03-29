package com.fujitsu.delivery.exception;

/** Exception thrown when weather conditions forbid the usage of a specific vehicle type. */
public class VehicleForbiddenException extends RuntimeException {
  public VehicleForbiddenException(String message) {

    super(message);
  }
}
