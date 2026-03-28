package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeliveryFeeCalculationServiceTest {
  @Mock private WeatherDataRepository weatherDataRepository;

  @InjectMocks private DeliveryFeeCalculationService deliveryFeeCalculationService;

  @Test
  void calculateDeliveryFee_carInTallinn_returnsOnlyBaseFee() {
    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Car", null);

    assertEquals(4.0, response.getTotalFee());
    verify(weatherDataRepository, never())
        .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
            anyString(), any(LocalDateTime.class));
  }

  @Test
  void calculateDeliveryFee_bikeInTartu_withExampleCalculations() {
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

    assertEquals(4.0, response.getTotalFee());
  }

  @Test
  void calculateDeliveryFee_scooterInParnu_withRainAndCold() {
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

    assertEquals(4.0, response.getTotalFee());
  }

  @Test
  void calculateDeliveryFee_bikeInTallinn_forbiddenWindSpeed() {
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

  @Test
  void calculateDeliveryFee_scooterInTartu_forbiddenWeatherPhenomenon() {
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

  @Test
  void calculateDeliveryFee_missingWeatherData_throwsIllegalArgumentException() {
    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Pärnu"), any(LocalDateTime.class)))
        .thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Bike", null));

    org.junit.jupiter.api.Assertions.assertTrue(
        exception.getMessage().startsWith("Weather data not found for city: Pärnu"));
  }

  @Test
  void calculateDeliveryFee_invalidCity_throwsIllegalArgumentException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Võru", "Car", null));

    assertEquals("Invalid city: VÕRU", exception.getMessage());
  }

  @Test
  void calculateDeliveryFee_bikeInTallinn_withHistoricalTimestamp_rainyWeather() {
    LocalDateTime historicalTime = LocalDateTime.parse("2026-03-27T10:00:00");
    WeatherData historicalWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0) // Tavaline temperatuur (ei anna lisatasu)
            .windSpeed(4.0) // Tavaline tuul (ei anna lisatasu)
            .weatherPhenomenon("Light rain") // Vihm! (Annab jalgrattale +0.5€)
            .build();

    when(weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                eq("Tallinn-Harku"), eq(historicalTime)))
        .thenReturn(Optional.of(historicalWeather));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike", historicalTime);

    assertEquals(3.5, response.getTotalFee());
  }
}
