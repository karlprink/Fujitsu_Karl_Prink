package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class WindExtraFeeStrategy implements WeatherExtraFeeStrategy {

    @Override
    public boolean supports(String vehicleType) {
        return vehicleType.equalsIgnoreCase("BIKE");
    }

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