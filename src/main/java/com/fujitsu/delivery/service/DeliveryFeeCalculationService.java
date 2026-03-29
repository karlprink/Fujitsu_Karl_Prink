package com.fujitsu.delivery.service;

import com.fujitsu.delivery.adapter.WeatherDataAdapter;
import com.fujitsu.delivery.dto.DeliveryFeeResponse;
import com.fujitsu.delivery.entity.BaseFee;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.repository.BaseFeeRepository;
import com.fujitsu.delivery.strategy.WeatherExtraFeeStrategy;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Core business service responsible for calculating the total delivery fee. It orchestrates the
 * retrieval of regional base fees and delegates weather-based surcharge calculations to a
 * collection of specialized strategies.
 */
@Service
@RequiredArgsConstructor
public class DeliveryFeeCalculationService {

  private final BaseFeeRepository baseFeeRepository;
  private final WeatherDataAdapter weatherDataAdapter;
  private final CityStationService cityStationService;
  private final List<WeatherExtraFeeStrategy> weatherExtraFeeStrategies;

  /**
   * Calculates the final delivery fee for a specific city and vehicle type. The calculation process
   * includes looking up the regional base fee, resolving the appropriate weather station, and
   * applying all applicable weather surcharges (e.g., wind, temperature, phenomena).
   *
   * @param city The delivery destination city.
   * @param vehicleTypeStr The type of vehicle used for delivery.
   * @param timestamp Optional timestamp for historical calculations; defaults to the current time
   *     if null.
   * @return A {@link DeliveryFeeResponse} containing the summed total fee.
   * @throws IllegalArgumentException if no base fee rule or city-station mapping is found, or if
   *     weather conditions forbid the delivery.
   */
  public DeliveryFeeResponse calculateDeliveryFee(
      String city, String vehicleTypeStr, LocalDateTime timestamp) {
    String cityUpper = city.toUpperCase();
    String vehicleUpper = vehicleTypeStr.toUpperCase();
    LocalDateTime targetTime = (timestamp != null) ? timestamp : LocalDateTime.now();

    double baseFee = calculateRegionalBaseFee(cityUpper, vehicleUpper);

    List<WeatherExtraFeeStrategy> applicableStrategies =
        weatherExtraFeeStrategies.stream()
            .filter(strategy -> strategy.supports(vehicleUpper))
            .toList();

    if (applicableStrategies.isEmpty()) {
      return new DeliveryFeeResponse(baseFee);
    }

    String stationName =
        cityStationService
            .getStationNameByCity(cityUpper)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Weather station mapping not found for city: " + cityUpper));

    WeatherData latestWeather = weatherDataAdapter.getLatestWeatherData(stationName, targetTime);

    double extraFee =
        applicableStrategies.stream()
            .mapToDouble(strategy -> strategy.execute(latestWeather, vehicleUpper))
            .sum();

    return new DeliveryFeeResponse(baseFee + extraFee);
  }

  private double calculateRegionalBaseFee(String city, String vehicle) {
    BaseFee baseFeeRule =
        baseFeeRepository
            .findByCityIgnoreCaseAndVehicleTypeIgnoreCase(city, vehicle)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Base fee rule not found for city: " + city + " and vehicle: " + vehicle));

    return baseFeeRule.getFee();
  }
}
