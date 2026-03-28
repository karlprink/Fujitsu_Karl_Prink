package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the DeliveryFeeCalculationService. Verifies the business logic for calculating
 * delivery fees based on regional rules, vehicle types, and weather conditions.
 */
@ExtendWith(MockitoExtension.class)
class DeliveryFeeCalculationServiceTest {

  @Mock private WeatherDataRepository weatherDataRepository;

  @Mock private BaseFeeRepository baseFeeRepository;

  @InjectMocks private DeliveryFeeCalculationService deliveryFeeCalculationService;

  /** Helper method to create a BaseFee mock object. */
  private BaseFee createBaseFee(String city, String vehicle, double fee) {
    return BaseFee.builder().city(city).vehicleType(vehicle).fee(fee).build();
  }

  /**
   * Tests that if the vehicle is a car, only the base fee is applied. Weather conditions should not
   * be checked or added to the total fee.
   */
  @Test
  void calculateDeliveryFee_carInTallinn_returnsOnlyBaseFee() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "CAR"))
        .thenReturn(Optional.of(createBaseFee("TALLINN", "CAR", 4.0)));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Car", null);

    assertEquals(4.0, response.getTotalFee());
    // Verify that the weather repository is never called for cars
    verify(weatherDataRepository, never())
        .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
            anyString(), any(LocalDateTime.class));
  }

  /**
   * Tests a standard delivery fee calculation for a bike in Tartu. Verifies that weather conditions
   * (like snow) are correctly added to the base fee.
   */
  @Test
  void calculateDeliveryFee_bikeInTartu_withExampleCalculations() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TARTU", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("TARTU", "BIKE", 2.5)));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tartu-Tõravere")
            .airTemperature(-2.1)
            .windSpeed(4.7)
            .weatherPhenomenon("Light snow shower")
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Tartu-Tõravere"), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockWeather));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Bike", null);

    // 2.5 (base) + 0.5 (temp between 0 and -10) + 1.0 (snow) = 4.0
    assertEquals(4.0, response.getTotalFee());
  }

  /**
   * Tests a delivery fee calculation for a scooter in Pärnu under harsh weather conditions.
   * Verifies that both cold temperature and rain surcharges are correctly applied.
   */
  @Test
  void calculateDeliveryFee_scooterInParnu_withRainAndCold() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("PÄRNU", "SCOOTER"))
        .thenReturn(Optional.of(createBaseFee("PÄRNU", "SCOOTER", 2.5)));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Pärnu")
            .airTemperature(-12.0)
            .windSpeed(5.0)
            .weatherPhenomenon("Heavy rain")
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Pärnu"), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockWeather));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Scooter", null);

    // 2.5 (base) + 1.0 (temp < -10) + 0.5 (rain) = 4.0
    assertEquals(4.0, response.getTotalFee());
  }

  /**
   * Tests the restriction rules for bikes. Verifies that a VehicleForbiddenException is thrown if
   * the wind speed exceeds 20 m/s.
   */
  @Test
  void calculateDeliveryFee_bikeInTallinn_forbiddenWindSpeed() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("TALLINN", "BIKE", 3.0)));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0)
            .windSpeed(21.5)
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Tallinn-Harku"), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockWeather));

    VehicleForbiddenException exception =
        assertThrows(
            VehicleForbiddenException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike", null));

    assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());
  }

  /**
   * Tests the restriction rules for scooters. Verifies that a VehicleForbiddenException is thrown
   * for forbidden weather phenomena (e.g., thunderstorm).
   */
  @Test
  void calculateDeliveryFee_scooterInTartu_forbiddenWeatherPhenomenon() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TARTU", "SCOOTER"))
        .thenReturn(Optional.of(createBaseFee("TARTU", "SCOOTER", 3.0)));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tartu-Tõravere")
            .airTemperature(10.0)
            .windSpeed(2.0)
            .weatherPhenomenon("Thunderstorm")
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Tartu-Tõravere"), any(LocalDateTime.class)))
        .thenReturn(Optional.of(mockWeather));

    VehicleForbiddenException exception =
        assertThrows(
            VehicleForbiddenException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Scooter", null));

    assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());
  }

  /**
   * Tests the edge case where weather data is not found in the database. Verifies that an
   * IllegalArgumentException is thrown with a descriptive message.
   */
  @Test
  void calculateDeliveryFee_missingWeatherData_throwsIllegalArgumentException() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("PÄRNU", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("PÄRNU", "BIKE", 2.0)));

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Pärnu"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Bike", null));

    assertTrue(exception.getMessage().startsWith("Weather data not found for city: Pärnu"));
  }

  /**
   * Tests the edge case where a fee rule cannot be found for the given city and vehicle
   * combination. Verifies that an IllegalArgumentException is thrown.
   */
  @Test
  void calculateDeliveryFee_invalidCity_throwsIllegalArgumentException() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("VÕRU", "CAR"))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Võru", "Car", null));

    assertEquals("Base fee rule not found for city: VÕRU and vehicle: CAR", exception.getMessage());
  }

  /**
   * Tests the historical fee calculation feature. Verifies that the service correctly utilizes the
   * provided timestamp parameter to fetch past weather data and applies the correct surcharges.
   */
  @Test
  void calculateDeliveryFee_bikeInTallinn_withHistoricalTimestamp_rainyWeather() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("TALLINN", "BIKE", 3.0)));

    LocalDateTime historicalTime = LocalDateTime.parse("2026-03-27T10:00:00");
    WeatherData historicalWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0)
            .windSpeed(4.0)
            .weatherPhenomenon("Light rain")
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Tallinn-Harku"), eq(historicalTime)))
        .thenReturn(Optional.of(historicalWeather));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike", historicalTime);

    // 3.0 (base) + 0.5 (rain) = 3.5
    assertEquals(3.5, response.getTotalFee());
  }
}
