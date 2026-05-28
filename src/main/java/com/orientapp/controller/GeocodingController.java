package com.orientapp.controller;

import com.orientapp.dto.GeocodingResultDto;
import com.orientapp.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API do geokodowania – konwertuje nazwy miejscowości na współrzędne GPS.
 * Używany przez formularz tworzenia zawodów.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GeocodingController {

    private final GeocodingService geocodingService;

    /**
     * Wyszukuje współrzędne geograficzne dla podanej nazwy miejscowości.
     *
     * @param q zapytanie, np. "Jelenia Góra"
     * @return lista wyników JSON z polami lat, lon, displayName
     */
    @GetMapping("/geocode")
    public ResponseEntity<List<GeocodingResultDto>> geocode(@RequestParam String q) {
        if (q == null || q.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(geocodingService.search(q));
    }
}
