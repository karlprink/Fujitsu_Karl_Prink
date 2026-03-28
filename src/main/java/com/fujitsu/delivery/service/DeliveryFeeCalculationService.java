package com.fujitsu.delivery.service;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryFeeCalculationService {
  private final WeatherDataRepository weatherDataRepository;
    private final BaseFeeRepository baseFeeRepository;

  /**
   * Calculates the total delivery fee based on city, vehicle type, and current weather conditions.
   *
   * @param city The target city for delivery (Tallinn, Tartu, Pärnu)
   * @param vehicleType The type of vehicle used (Car, Scooter, Bike)
   * @return DeliveryFeeDTO containing the calculated fee
   */
  public DeliveryFeeDTO calculateDeliveryFee(
      String city, String vehicleType, LocalDateTime timestamp) {
    String cityUpper = city.toUpperCase();
    String vehicleUpper = vehicleType.toUpperCase();

    // If timestamp is null, use current time
    LocalDateTime targetTime = (timestamp != null) ? timestamp : LocalDateTime.now();

    double baseFee = calculateRegionalBaseFee(cityUpper, vehicleUpper);

    if (vehicleUpper.equals("CAR")) {
      return new DeliveryFeeDTO(baseFee);
    }

    // Fetch data from database
    String stationName = getStationNameByCity(cityUpper);
    WeatherData latestWeather =
        weatherDataRepository
            .findFirstByStationNameAndObservationTimestampLessThanEqualOrderByObservationTimestampDesc(
                stationName, targetTime)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Weather data not found for city: " + city + " at " + targetTime));

    double extraFee = calculateExtraWeatherFees(latestWeather, vehicleUpper);

    return new DeliveryFeeDTO(baseFee + extraFee);
  }

    private double calculateRegionalBaseFee(String city, String vehicle) {
        BaseFee baseFeeRule = baseFeeRepository
                .findByCityIgnoreCaseAndVehicleTypeIgnoreCase(city, vehicle)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Base fee rule not found for city: " + city + " and vehicle: " + vehicle));

        return baseFeeRule.getFee();
    }

  private double calculateExtraWeatherFees(WeatherData latestWeather, String vehicleUpper) {
    double extraFee = 0.0;

    // Temperature(ATEF) - Scooter and Bike
    if (latestWeather.getAirTemperature() < -10.0) {
      extraFee += 1.0;
    } else if (latestWeather.getAirTemperature() >= -10.0
        && latestWeather.getAirTemperature() <= 0.0) {
      extraFee += 0.5;
    }

    // Wind (WSEF) - only Bike
    if ("BIKE".equals(vehicleUpper)) {
      if (latestWeather.getWindSpeed() > 20.0) {
        throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
      } else if (latestWeather.getWindSpeed() >= 10.0) {
        extraFee += 0.5;
      }
    }

    // Phenomen (WPEF) - Scooter and Bike
    String phenomenon =
        latestWeather.getWeatherPhenomenon() != null
            ? latestWeather.getWeatherPhenomenon().toLowerCase()
            : "";
    if (phenomenon.contains("glaze")
        || phenomenon.contains("hail")
        || phenomenon.contains("thunder")) {
      throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
    } else if (phenomenon.contains("snow") || phenomenon.contains("sleet")) {
      extraFee += 1.0;
    } else if (phenomenon.contains("rain") || phenomenon.contains("shower")) {
      extraFee += 0.5;
    }

    return extraFee;
  }

  private String getStationNameByCity(String city) {
    return switch (city) {
      case "TALLINN" -> "Tallinn-Harku";
      case "TARTU" -> "Tartu-Tõravere";
      case "PÄRNU", "PARNU" -> "Pärnu";
      default -> throw new IllegalArgumentException("Invalid city: " + city);
    };
  }
}
