package com.orientapp.dto;

/**
 * Wynik geokodowania zwracany przez Nominatim OpenStreetMap API.
 *
 * @param lat         szerokość geograficzna znalezionej lokalizacji
 * @param lon         długość geograficzna znalezionej lokalizacji
 * @param displayName pełna, czytelna nazwa lokalizacji
 */
public record GeocodingResultDto(double lat, double lon, String displayName) {}
