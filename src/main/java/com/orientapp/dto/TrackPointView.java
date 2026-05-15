package com.orientapp.dto;

public record TrackPointView(Long id, Double lat, Double lng, String timestamp, Integer checkpointOrder) {}
