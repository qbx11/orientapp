package com.orientapp.dto;

/**
 * Lekki widok punktu trasy do prezentacji na mapie (np. jako JSON).
 *
 * @param id              identyfikator punktu trasy
 * @param lat             szerokość geograficzna
 * @param lng             długość geograficzna
 * @param timestamp       znacznik czasu w postaci tekstowej
 * @param checkpointOrder kolejność punktu kontrolnego (może być {@code null})
 */
public record TrackPointView(Long id, Double lat, Double lng, String timestamp, Integer checkpointOrder) {}
