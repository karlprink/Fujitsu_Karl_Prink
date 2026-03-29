package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Weather surcharge strategy based on wind speed. This strategy is uniquely applicable to Bikes, as
 * strong winds significantly affect safety and effort for cycling deliveries.
 */
@Component
public class WindExtraFeeStrategy implements WeatherExtraFeeStrategy {
  /**
   * Determines if the wind-based surcharge is applicable. According to business rules, this
   * currently only supports Bike deliveries.
   *
   * @param vehicleType The normalized vehicle type (e.g., "BIKE").
   * @return {@code true} if the vehicle type is "BIKE", {@code false} otherwise.
   */
  @Override
  public boolean supports(String vehicleType) {
    return vehicleType.equalsIgnoreCase("BIKE");
  }

  /**
   * Evaluates the wind speed and calculates the corresponding surcharge or throws an exception if
   * conditions are unsafe.
   *
   * <ul>
   *   <li>Wind speed between 10 m/s and 20 m/s: 0.50 € extra
   *   <li>Wind speed &gt; 20 m/s: Delivery forbidden
   *   <li>Wind speed &lt; 10 m/s: No extra fee
   * </ul>
   *
   * @param latestWeather The weather observation record containing wind speed data.
   * @param vehicleType The type of vehicle for context.
   * @return The calculated wind-based surcharge in Euros.
   * @throws VehicleForbiddenException If wind speed exceeds the safety threshold of 20 m/s.
   */
  @Override
  public Double execute(WeatherData latestWeather, String vehicleType) {
    double windSpeed = latestWeather.getWindSpeed();

    validateWindSpeed(windSpeed);

    if (windSpeed >= 10.0 && windSpeed <= 20.0) {
      return 0.5;
    }

    return 0.0;
  }

  private void validateWindSpeed(double windSpeed) {
    if (windSpeed > 20.0) {
      throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
    }
  }
}
