package com.orientapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO formularza dodawania kategorii (trasy) do zawodów.
 * Walidacja Bean Validation gwarantuje obecność nazwy i dodatni dystans.
 */
@Data
public class CategoryFormDto {

    /** Nazwa kategorii (wymagana, maks. 50 znaków). */
    @NotBlank(message = "Nazwa kategorii jest wymagana")
    @Size(max = 50, message = "Nazwa kategorii może mieć maksymalnie 50 znaków")
    private String name;

    /** Długość trasy w kilometrach (opcjonalna, większa niż 0). */
    @DecimalMin(value = "0.1", message = "Dystans musi być większy niż 0")
    private Double distanceKm;
}
