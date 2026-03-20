package com.fujitsu.delivery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

public class WeatherDataXmlDto {
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
