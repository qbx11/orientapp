package com.orientapp.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * DTO formularza dodawania punktu trasy GPS do zgłoszenia.
 * Walidacja Bean Validation pilnuje poprawnych zakresów współrzędnych geograficznych.
 */
@Data
public class TrackPointFormDto {

    /** Szerokość geograficzna punktu (wymagana, zakres -90…90). */
    @NotNull(message = "Szerokość geograficzna jest wymagana")
    @DecimalMin(value = "-90.0", message = "Szerokość musi być >= -90")
    @DecimalMax(value = "90.0",  message = "Szerokość musi być <= 90")
    private Double latitude;

    /** Długość geograficzna punktu (wymagana, zakres -180…180). */
    @NotNull(message = "Długość geograficzna jest wymagana")
    @DecimalMin(value = "-180.0", message = "Długość musi być >= -180")
    @DecimalMax(value = "180.0",  message = "Długość musi być <= 180")
    private Double longitude;

    /** Znacznik czasu osiągnięcia punktu (wymagany). */
    @NotNull(message = "Czas osiągnięcia jest wymagany")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime timestamp;

    /** Kolejność punktu kontrolnego na trasie (opcjonalna). */
    private Integer checkpointOrder;
}
