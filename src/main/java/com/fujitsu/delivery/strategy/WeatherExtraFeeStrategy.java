package com.fujitsu.delivery.strategy;

import com.fujitsu.delivery.entity.WeatherData;

public interface WeatherExtraFeeStrategy {
    boolean supports(String vehicleType);

    Double execute(WeatherData latestWeather, String vehicleType);
}