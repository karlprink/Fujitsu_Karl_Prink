package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.constant.PhenomenonConstants;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import org.springframework.stereotype.Component;

/**
 * Weather surcharge strategy based on specific weather phenomena (e.g., snow, rain, glaze). This
 * strategy determines additional fees or usage restrictions for weather-sensitive vehicles like
 * Scooters and Bikes.
 */
@Component
public class PhenomenonExtraFeeStrategy implements WeatherExtraFeeStrategy {
  /**
   * Determines if this strategy applies to the given vehicle type.
   *
   * @param vehicleType The normalized vehicle type (e.g., "SCOOTER", "BIKE").
   * @return {@code true} if the vehicle is sensitive to weather phenomena, {@code false} otherwise.
   */
  @Override
  public boolean supports(String vehicleType) {
    return vehicleType.equalsIgnoreCase("SCOOTER") || vehicleType.equalsIgnoreCase("BIKE");
  }

  /**
   * Evaluates the current weather phenomenon and calculates the corresponding surcharge. If weather
   * conditions are extreme (e.g., thunder, hail, glaze), it forbids delivery.
   *
   * @param latestWeather The weather observation record to evaluate.
   * @param vehicleType The type of vehicle for context-specific logic.
   * @return The calculated phenomenon-based surcharge in Euros (e.g., 0.5 or 1.0).
   * @throws VehicleForbiddenException If the phenomenon poses a safety risk for the vehicle.
   */
  @Override
  public Double execute(WeatherData latestWeather, String vehicleType) {
    String phenomenon =
        latestWeather.getWeatherPhenomenon() != null
            ? latestWeather.getWeatherPhenomenon().toLowerCase()
            : "";

    if (phenomenon.contains(PhenomenonConstants.GLAZE)
        || phenomenon.contains(PhenomenonConstants.HAIL)
        || phenomenon.contains(PhenomenonConstants.THUNDER)) {
      throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
    }

    if (phenomenon.contains(PhenomenonConstants.SNOW)
        || phenomenon.contains(PhenomenonConstants.SLEET)) {
      return 1.0;
    } else if (phenomenon.contains(PhenomenonConstants.RAIN)
        || phenomenon.contains(PhenomenonConstants.SHOWER)) {
      return 0.5;
    }

    return 0.0;
  }
}
