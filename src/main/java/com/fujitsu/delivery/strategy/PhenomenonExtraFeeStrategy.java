package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.constant.PhenomenonConstants;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class PhenomenonExtraFeeStrategy implements WeatherExtraFeeStrategy {

    @Override
    public boolean supports(String vehicleType) {
        return vehicleType.equalsIgnoreCase("SCOOTER") || vehicleType.equalsIgnoreCase("BIKE");
    }

    @Override
    public Double execute(WeatherData latestWeather, String vehicleType) {
        String phenomenon = latestWeather.getWeatherPhenomenon() != null
                ? latestWeather.getWeatherPhenomenon().toLowerCase()
                : "";

        if (phenomenon.contains(PhenomenonConstants.GLAZE)
                || phenomenon.contains(PhenomenonConstants.HAIL)
                || phenomenon.contains(PhenomenonConstants.THUNDER)) {
            throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
        }

        if (phenomenon.contains(PhenomenonConstants.SNOW) || phenomenon.contains(PhenomenonConstants.SLEET)) {
            return 1.0;
        }
        else if (phenomenon.contains(PhenomenonConstants.RAIN) || phenomenon.contains(PhenomenonConstants.SHOWER)) {
            return 0.5;
        }

        return 0.0;
    }
}