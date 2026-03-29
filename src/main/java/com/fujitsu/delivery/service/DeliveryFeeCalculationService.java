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

@Service
@RequiredArgsConstructor
public class DeliveryFeeCalculationService {

  private final BaseFeeRepository baseFeeRepository;
  private final WeatherDataAdapter weatherDataAdapter;
  private final CityStationService cityStationService;
  private final List<WeatherExtraFeeStrategy> weatherExtraFeeStrategies;

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
