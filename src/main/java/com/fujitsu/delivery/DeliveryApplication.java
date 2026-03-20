package com.fujitsu.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@SpringBootApplication
@EnableScheduling
public class DeliveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);

    }

    @Bean
    public CommandLineRunner testRunner(
            com.fujitsu.delivery.repository.WeatherDataRepository repository,
            com.fujitsu.delivery.service.DeliveryFeeCalculationService feeService) {
        return args -> {
            com.fujitsu.delivery.entity.WeatherData mockWeather = com.fujitsu.delivery.entity.WeatherData.builder()
                    .stationName("Tartu-Tõravere")
                    .wmoCode("26242")
                    .airTemperature(-2.1)
                    .windSpeed(4.7)
                    .weatherPhenomenon("Light snow shower")
                    .observationTimestamp(java.time.LocalDateTime.now())
                    .build();

            repository.save(mockWeather);
            log.info("Database injection");
            log.info("Delivert fee: TARTU and BIKE");
            com.fujitsu.delivery.dto.DeliveryFeeDTO response = feeService.calculateDeliveryFee("Tartu", "Bike");
            log.info("total:" + response.getTotalFee());
        };
    }
}
