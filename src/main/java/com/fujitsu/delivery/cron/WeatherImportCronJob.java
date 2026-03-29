package com.fujitsu.delivery.cron;

import com.fujitsu.delivery.service.WeatherImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Orchestrator for weather data import events. Handles both scheduled periodic updates and
 * application startup checks.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherImportCronJob {

  private final WeatherImportService weatherImportService;

  /**
   * Periodically triggers the weather data import based on the cron expression defined in the
   * application configuration.
   */
  @Scheduled(cron = "${weather.import.cron}")
  public void runScheduledImport() {
    log.info("Running scheduled cron job for weather data import.");
    weatherImportService.importWeatherData();
  }

  /**
   * Triggers a check and potential data import immediately after the application context is fully
   * initialized.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void runStartupImport() {
    log.info("Application context is ready. Invoking startup check for weather data.");
    weatherImportService.checkAndImportDataOnStartup();
  }
}
