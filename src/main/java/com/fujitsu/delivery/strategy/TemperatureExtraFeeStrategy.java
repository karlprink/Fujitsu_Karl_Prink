package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.entity.WeatherData;
import org.springframework.stereotype.Component;

@Component
public class TemperatureExtraFeeStrategy implements WeatherExtraFeeStrategy {

    @Override
    public boolean supports(String vehicleType) {
        return vehicleType.equalsIgnoreCase("SCOOTER") || vehicleType.equalsIgnoreCase("BIKE");
    }

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