package com.fujitsu.delivery.service;

import com.fujitsu.delivery.dto.DeliveryFeeDTO;
import com.fujitsu.delivery.entity.WeatherData;
import com.fujitsu.delivery.exception.VehicleForbiddenException;
import com.fujitsu.delivery.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryFeeCalculationService {
    private final WeatherDataRepository weatherDataRepository;

    /**
     * Calculates the total delivery fee based on city, vehicle type, and current weather conditions.
     *
     * @param city        The target city for delivery (Tallinn, Tartu, Pärnu)
     * @param vehicleType The type of vehicle used (Car, Scooter, Bike)
     * @return DeliveryFeeDTO containing the calculated fee
     */
    public DeliveryFeeDTO calculateDeliveryFee(String city, String vehicleType) {
        String cityUpper = city.toUpperCase();
        String vehicleUpper = vehicleType.toUpperCase();

        // Regional base fee
        double baseFee = calculateRegionalBaseFee(cityUpper, vehicleUpper);

        if (vehicleUpper.equals("CAR")) {
            return new DeliveryFeeDTO(baseFee);
        }
        // Fetch data from database
        String stationName = getStationNameByCity(cityUpper);
        WeatherData latestWeather = weatherDataRepository.findFirstByStationNameOrderByObservationTimestampDesc(stationName)
                .orElseThrow(() -> new IllegalArgumentException("Weather data not found for city: " + city));

        double extraFee = calculateExtraWeatherFees(latestWeather, vehicleUpper);

        return new DeliveryFeeDTO(baseFee + extraFee);
    }


    private double calculateRegionalBaseFee(String city, String vehicle) {
        return switch (city) {
            case "TALLINN" -> switch (vehicle) {
                case "CAR" -> 4.0;
                case "SCOOTER" -> 3.5;
                case "BIKE" -> 3.0;
                default -> throw new IllegalArgumentException("Invalid vehicle type: " + vehicle);
            };
            case "TARTU" -> switch (vehicle) {
                case "CAR" -> 3.5;
                case "SCOOTER" -> 3.0;
                case "BIKE" -> 2.5;
                default -> throw new IllegalArgumentException("Invalid vehicle type: " + vehicle);
            };
            case "PÄRNU", "PARNU" -> switch (vehicle) {
                case "CAR" -> 3.0;
                case "SCOOTER" -> 2.5;
                case "BIKE" -> 2.0;
                default -> throw new IllegalArgumentException("Invalid vehicle type: " + vehicle);
            };
            default -> throw new IllegalArgumentException("Invalid city: " + city);
        };
    }

    private double calculateExtraWeatherFees(WeatherData latestWeather, String vehicleUpper) {
        double extraFee = 0.0;

        // Temperature(ATEF) - Scooter and Bike
        if (latestWeather.getAirTemperature() < -10.0) {
            extraFee += 1.0;
        } else if (latestWeather.getAirTemperature() >= -10.0 && latestWeather.getAirTemperature() <= 0.0) {
            extraFee += 0.5;
        }

        // Wind (WSEF) - only Bike
        if ("BIKE".equals(vehicleUpper)) {
            if (latestWeather.getWindSpeed() > 20.0) {
                throw new VehicleForbiddenException("Usage of selected vehicle type is forbidden");
            } else if (latestWeather.getWindSpeed() >= 10.0 && latestWeather.getWindSpeed() <= 20.0) {
                extraFee += 0.5;
            }
        }

        // Phenomen (WPEF) - Scooter and Bike
        String phenomenon = latestWeather.getWeatherPhenomenon() != null ? latestWeather.getWeatherPhenomenon().toLowerCase() : "";
        if (phenomenon.contains("glaze") || phenomenon.contains("hail") || phenomenon.contains("thunder")) {
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

