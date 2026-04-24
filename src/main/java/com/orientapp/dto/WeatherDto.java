package com.orientapp.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WeatherDto {
    String description;
    Double tempCelsius;
    Integer humidity;
    Double windSpeed;
    String iconCode;

    public String iconUrl() {
        return "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
    }
}
