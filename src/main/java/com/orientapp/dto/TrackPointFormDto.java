package com.orientapp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class TrackPointFormDto {

    @NotNull(message = "Szerokość geograficzna jest wymagana")
    @DecimalMin(value = "-90.0", message = "Szerokość musi być >= -90")
    @DecimalMax(value = "90.0",  message = "Szerokość musi być <= 90")
    private Double latitude;

    @NotNull(message = "Długość geograficzna jest wymagana")
    @DecimalMin(value = "-180.0", message = "Długość musi być >= -180")
    @DecimalMax(value = "180.0",  message = "Długość musi być <= 180")
    private Double longitude;

    @NotNull(message = "Czas osiągnięcia jest wymagany")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime timestamp;

    private Integer checkpointOrder;
}
