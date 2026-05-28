package com.orientapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orientapp.dto.GeocodingResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis geokodowania – tłumaczy nazwy miejscowości na współrzędne geograficzne
 * przy użyciu Nominatim OpenStreetMap API (JSON).
 */
@Service
@Slf4j
public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search?q=%s&format=json&limit=5&addressdetails=0";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    /**
     * Konstruktor produkcyjny — tworzy domyślnego klienta HTTP.
     *
     * @param objectMapper mapper JSON używany do parsowania odpowiedzi
     */
    @Autowired
    public GeocodingService(ObjectMapper objectMapper) {
        this(objectMapper, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build());
    }

    /**
     * Konstruktor pozwalający wstrzyknąć klienta HTTP (np. mock w testach).
     *
     * @param objectMapper mapper JSON używany do parsowania odpowiedzi
     * @param httpClient   klient HTTP wykonujący zapytania do Nominatim
     */
    GeocodingService(ObjectMapper objectMapper, HttpClient httpClient) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
    }

    /**
     * Wyszukuje współrzędne dla podanej nazwy miejscowości.
     *
     * @param query nazwa miejscowości, np. "Jelenia Góra"
     * @return lista wyników (max 5), pusta lista gdy API niedostępne lub brak wyników
     */
    public List<GeocodingResultDto> search(String query) {
        List<GeocodingResultDto> results = new ArrayList<>();
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = String.format(NOMINATIM_URL, encoded);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "OrientApp/1.0 (university project)")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(8))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode nodes = objectMapper.readTree(response.body());
            for (JsonNode node : nodes) {
                double lat = node.get("lat").asDouble();
                double lon = node.get("lon").asDouble();
                String name = node.get("display_name").asText();
                results.add(new GeocodingResultDto(lat, lon, name));
            }
        } catch (Exception e) {
            log.warn("Geokodowanie nieudane dla zapytania '{}': {}", query, e.getMessage());
        }
        return results;
    }
}
