package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.entity.WeatherData;
import org.springframework.stereotype.Component;

/**
 * Weather surcharge strategy based on air temperature. This strategy applies additional fees to
 * weather-sensitive vehicles (Scooters and Bikes) when the outdoor temperature drops below freezing
 * levels to compensate for more difficult delivery conditions.
 */
@Component
public class TemperatureExtraFeeStrategy implements WeatherExtraFeeStrategy {
  /**
   * Determines if the temperature-based surcharge is applicable for the specified vehicle.
   *
   * @param vehicleType The normalized vehicle type (e.g., "SCOOTER", "BIKE").
   * @return {@code true} if the vehicle is affected by temperature surcharges, {@code false}
   *     otherwise.
   */
  @Override
  public boolean supports(String vehicleType) {
    return vehicleType.equalsIgnoreCase("SCOOTER") || vehicleType.equalsIgnoreCase("BIKE");
  }

  /**
   * Calculates the extra fee based on the recorded air temperature.
   *
   * <ul>
   *   <li>Temp &lt; -10°C: 1.00 € extra
   *   <li>Temp between -10°C and 0°C: 0.50 € extra
   *   <li>Temp &gt; 0°C: No extra fee
   * </ul>
   *
   * @param latestWeather The weather observation record containing the air temperature.
   * @param vehicleType The type of vehicle (not used for specific logic here but required by
   *     interface).
   * @return The calculated temperature-based surcharge in Euros.
   */
  @Override
  public Double execute(WeatherData latestWeather, String vehicleType) {
    double temp = latestWeather.getAirTemperature();

    if (temp < -10.0) {
      return 1.0;
    } else if (temp >= -10.0 && temp <= 0.0) {
      return 0.5;
    }

    return 0.0;
  }
}
