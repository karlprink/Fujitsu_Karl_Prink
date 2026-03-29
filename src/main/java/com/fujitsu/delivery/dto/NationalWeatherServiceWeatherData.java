package com.fujitsu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.List;
import lombok.Data;

/**
 * Data Transfer Object for mapping the XML response from the National Weather Service. This class
 * structure corresponds to the official Estonian Environment Agency weather observation format.
 */
public class NationalWeatherServiceWeatherData {
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JacksonXmlRootElement(localName = "observations")
  public static class Observations {
    @JacksonXmlProperty(isAttribute = true)
    private Long timestamp;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "station")
    private List<Station> stations;
  }

  /**
   * Represents an individual "station" element within the XML. Maps specific weather metrics
   * required for the delivery fee calculation.
   */
  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Station {
    private String name;
    private String wmocode;

    @JacksonXmlProperty(localName = "airtemperature")
    private Double airTemperature;

    @JacksonXmlProperty(localName = "windspeed")
    private Double windSpeed;

    @JacksonXmlProperty(localName = "phenomenon")
    private String weatherPhenomenon;
  }
}
