package com.orientapp.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.orientapp.dto.WeatherDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class WeatherService {

    private static final String OWM_URL =
            "https://api.openweathermap.org/data/2.5/weather" +
            "?lat={lat}&lon={lon}&appid={key}&units=metric&lang=pl";

    @Value("${openweathermap.api.key:}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();

    /**
     * Pobiera aktualną pogodę dla podanych współrzędnych.
     * Zwraca Optional.empty() gdy klucz API nie jest skonfigurowany lub serwis niedostępny.
     */
    public Optional<WeatherDto> getWeather(Double lat, Double lon) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("OpenWeatherMap API key not configured — skipping weather fetch");
            return Optional.empty();
        }
        if (lat == null || lon == null) {
            return Optional.empty();
        }
        try {
            OWMResponse response = restClient.get()
                    .uri(OWM_URL, lat, lon, apiKey)
                    .retrieve()
                    .body(OWMResponse.class);

            if (response == null || response.main() == null) {
                return Optional.empty();
            }

            String description = (response.weather() != null && !response.weather().isEmpty())
                    ? response.weather().get(0).description() : "brak danych";
            String icon = (response.weather() != null && !response.weather().isEmpty())
                    ? response.weather().get(0).icon() : "01d";

            return Optional.of(WeatherDto.builder()
                    .description(description)
                    .tempCelsius(response.main().temp())
                    .humidity(response.main().humidity())
                    .windSpeed(response.wind() != null ? response.wind().speed() : null)
                    .iconCode(icon)
                    .build());

        } catch (Exception e) {
            log.warn("OpenWeatherMap API unavailable for ({}, {}): {}", lat, lon, e.getMessage());
            return Optional.empty();
        }
    }

    // ── Wewnętrzne rekordy do parsowania odpowiedzi OWM ──────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OWMResponse(List<OWMWeather> weather, OWMMain main, OWMWind wind) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OWMWeather(String description, String icon) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OWMMain(Double temp, Integer humidity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OWMWind(Double speed) {}
}
