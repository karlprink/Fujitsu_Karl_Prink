package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.adapter.WeatherDataAdapter;
import com.fujitsu.delivery.dto.DeliveryFeeResponse;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import com.fujitsu.delivery.strategy.PhenomenonExtraFeeStrategy;
import com.fujitsu.delivery.strategy.TemperatureExtraFeeStrategy;
import com.fujitsu.delivery.strategy.WeatherExtraFeeStrategy;
import com.fujitsu.delivery.strategy.WindExtraFeeStrategy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the DeliveryFeeCalculationService. Verifies the business logic for calculating
 * delivery fees using dynamic weather strategies, regional rules, and external data adapters.
 */
@ExtendWith(MockitoExtension.class)
class DeliveryFeeCalculationServiceTest {

  @Mock private BaseFeeRepository baseFeeRepository;
  @Mock private WeatherDataAdapter weatherDataAdapter;
  @Mock private CityStationService cityStationService;

  private DeliveryFeeCalculationService deliveryFeeCalculationService;

  @BeforeEach
  void setUp() {
    List<WeatherExtraFeeStrategy> realStrategies =
        List.of(
            new TemperatureExtraFeeStrategy(),
            new WindExtraFeeStrategy(),
            new PhenomenonExtraFeeStrategy());

    deliveryFeeCalculationService =
        new DeliveryFeeCalculationService(
            baseFeeRepository, weatherDataAdapter, cityStationService, realStrategies);
  }

  /** Helper method to create a BaseFee mock object. */
  private BaseFee createBaseFee(String city, String vehicle, double fee) {
    return BaseFee.builder().city(city).vehicleType(vehicle).fee(fee).build();
  }

  /**
   * Tests that if the vehicle is not affected by weather (e.g., CAR), only the base fee is applied.
   * Weather conditions and station mappings should not be checked at all.
   */
  @Test
  void calculateDeliveryFee_carInTallinn_returnsOnlyBaseFee() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "CAR"))
        .thenReturn(Optional.of(createBaseFee("TALLINN", "CAR", 4.0)));

    DeliveryFeeResponse response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Car", null);

    assertEquals(4.0, response.getTotalFee());

    verify(cityStationService, never()).getStationNameByCity(anyString());
    verify(weatherDataAdapter, never()).getLatestWeatherData(anyString(), any(LocalDateTime.class));
  }

  /**
   * Tests a standard delivery fee calculation for a bike in Tartu. Verifies that weather conditions
   * (like snow and temp) are correctly added to the base fee.
   */
  @Test
  void calculateDeliveryFee_bikeInTartu_withExampleCalculations() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TARTU", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("TARTU", "BIKE", 2.5)));
    when(cityStationService.getStationNameByCity("TARTU"))
        .thenReturn(Optional.of("Tartu-Tõravere"));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tartu-Tõravere")
            .airTemperature(-2.1)
            .windSpeed(4.7)
            .weatherPhenomenon("Light snow shower")
            .build();

    when(weatherDataAdapter.getLatestWeatherData(eq("Tartu-Tõravere"), any(LocalDateTime.class)))
        .thenReturn(mockWeather);

    DeliveryFeeResponse response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Bike", null);

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
    when(cityStationService.getStationNameByCity("PÄRNU")).thenReturn(Optional.of("Pärnu"));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Pärnu")
            .airTemperature(-12.0)
            .windSpeed(5.0)
            .weatherPhenomenon("Heavy rain")
            .build();

    when(weatherDataAdapter.getLatestWeatherData(eq("Pärnu"), any(LocalDateTime.class)))
        .thenReturn(mockWeather);

    DeliveryFeeResponse response =
        deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Scooter", null);

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
    when(cityStationService.getStationNameByCity("TALLINN"))
        .thenReturn(Optional.of("Tallinn-Harku"));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0)
            .windSpeed(21.5)
            .build();

    when(weatherDataAdapter.getLatestWeatherData(eq("Tallinn-Harku"), any(LocalDateTime.class)))
        .thenReturn(mockWeather);

    assertThrows(
        VehicleForbiddenException.class,
        () -> deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike", null));
  }

  /**
   * Tests the restriction rules for scooters. Verifies that a VehicleForbiddenException is thrown
   * for forbidden weather phenomena (e.g., thunder).
   */
  @Test
  void calculateDeliveryFee_scooterInTartu_forbiddenWeatherPhenomenon() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TARTU", "SCOOTER"))
        .thenReturn(Optional.of(createBaseFee("TARTU", "SCOOTER", 3.0)));
    when(cityStationService.getStationNameByCity("TARTU"))
        .thenReturn(Optional.of("Tartu-Tõravere"));

    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tartu-Tõravere")
            .airTemperature(10.0)
            .windSpeed(2.0)
            .weatherPhenomenon("Thunderstorm")
            .build();

    when(weatherDataAdapter.getLatestWeatherData(eq("Tartu-Tõravere"), any(LocalDateTime.class)))
        .thenReturn(mockWeather);

    assertThrows(
        VehicleForbiddenException.class,
        () -> deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Scooter", null));
  }

  /** Tests the edge case where weather data is not found in the database by the adapter. */
  @Test
  void calculateDeliveryFee_missingWeatherData_throwsIllegalArgumentException() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("PÄRNU", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("PÄRNU", "BIKE", 2.0)));
    when(cityStationService.getStationNameByCity("PÄRNU")).thenReturn(Optional.of("Pärnu"));

    when(weatherDataAdapter.getLatestWeatherData(eq("Pärnu"), any(LocalDateTime.class)))
        .thenThrow(new IllegalArgumentException("Weather data not found for station: Pärnu"));

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Bike", null));

    assertTrue(exception.getMessage().contains("Weather data not found"));
  }

  /** Tests the edge case where the city is not mapped to any weather station. */
  @Test
  void calculateDeliveryFee_unmappedCity_throwsIllegalArgumentException() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("VÕRU", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("VÕRU", "BIKE", 2.0)));

    when(cityStationService.getStationNameByCity("VÕRU")).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Võru", "Bike", null));

    assertEquals("Weather station mapping not found for city: VÕRU", exception.getMessage());
  }

  /**
   * Tests the edge case where a base fee rule cannot be found for the given city and vehicle
   * combination.
   */
  @Test
  void calculateDeliveryFee_invalidCityOrVehicle_throwsIllegalArgumentException() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("LONDON", "CAR"))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("London", "Car", null));

    assertEquals(
        "Base fee rule not found for city: LONDON and vehicle: CAR", exception.getMessage());
  }

  /** Tests the historical fee calculation feature using a specific timestamp parameter. */
  @Test
  void calculateDeliveryFee_bikeInTallinn_withHistoricalTimestamp() {
    when(baseFeeRepository.findByCityIgnoreCaseAndVehicleTypeIgnoreCase("TALLINN", "BIKE"))
        .thenReturn(Optional.of(createBaseFee("TALLINN", "BIKE", 3.0)));
    when(cityStationService.getStationNameByCity("TALLINN"))
        .thenReturn(Optional.of("Tallinn-Harku"));

    LocalDateTime historicalTime = LocalDateTime.parse("2026-03-27T10:00:00");
    WeatherData historicalWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0)
            .windSpeed(4.0)
            .weatherPhenomenon("Light rain")
            .build();

    when(weatherDataAdapter.getLatestWeatherData(eq("Tallinn-Harku"), eq(historicalTime)))
        .thenReturn(historicalWeather);

    DeliveryFeeResponse response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike", historicalTime);

    assertEquals(3.5, response.getTotalFee());
  }
}
