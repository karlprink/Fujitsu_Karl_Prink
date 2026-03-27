package com.fujitsu.delivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.WeatherDataRepository;
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
    DeliveryFeeDTO response = deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Car");

    assertEquals(4.0, response.getTotalFee());
    // Never turn to database when vehicle is "CAR"
    verify(weatherDataRepository, never())
        .findFirstByStationNameOrderByObservationTimestampDesc(anyString());
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

    when(weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc(
            "Tartu-Tõravere"))
        .thenReturn(Optional.of(mockWeather));

    DeliveryFeeDTO response = deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Bike");

    assertEquals(4.0, response.getTotalFee());
  }

  @Test
  void calculateDeliveryFee_scooterInParnu_withRainAndCold() {
    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Pärnu")
            .airTemperature(-12.0) // Colder than -10 (+1.0)
            .windSpeed(5.0) // Does not matter for Scooter
            .weatherPhenomenon("Heavy rain") // Rain (+0.5)
            .build();

    when(weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc("Pärnu"))
        .thenReturn(Optional.of(mockWeather));

    DeliveryFeeDTO response =
        deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Scooter");

    assertEquals(4.0, response.getTotalFee());
  }

  @Test
  void calculateDeliveryFee_bikeInTallinn_forbiddenWindSpeed() {
    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tallinn-Harku")
            .airTemperature(5.0)
            .windSpeed(21.5) // wind speed over 20m/s is forbidden for bike.
            .build();

    when(weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc(
            "Tallinn-Harku"))
        .thenReturn(Optional.of(mockWeather));

    VehicleForbiddenException exception =
        assertThrows(
            VehicleForbiddenException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Tallinn", "Bike"));

    assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());
  }

  @Test
  void calculateDeliveryFee_scooterInTartu_forbiddenWeatherPhenomenon() {
    WeatherData mockWeather =
        WeatherData.builder()
            .stationName("Tartu-Tõravere")
            .airTemperature(10.0)
            .windSpeed(2.0)
            .weatherPhenomenon("Thunderstorm") // forbidden for Scooter(also for bike)
            .build();

    when(weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc(
            "Tartu-Tõravere"))
        .thenReturn(Optional.of(mockWeather));

    VehicleForbiddenException exception =
        assertThrows(
            VehicleForbiddenException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Tartu", "Scooter"));

    assertEquals("Usage of selected vehicle type is forbidden", exception.getMessage());
  }

  @Test
  void calculateDeliveryFee_missingWeatherData_throwsIllegalArgumentException() {
    when(weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc("Pärnu"))
        .thenReturn(Optional.empty()); // Database is empty

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Pärnu", "Bike"));

    assertEquals("Weather data not found for city: Pärnu", exception.getMessage());
  }

  @Test
  void calculateDeliveryFee_invalidCity_throwsIllegalArgumentException() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> deliveryFeeCalculationService.calculateDeliveryFee("Võru", "Car"));

    assertEquals("Invalid city: VÕRU", exception.getMessage());
  }
}
