package com.orientapp.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryFormDto {

    @NotBlank(message = "Nazwa kategorii jest wymagana")
    @Size(max = 50, message = "Nazwa kategorii może mieć maksymalnie 50 znaków")
    private String name;

    @DecimalMin(value = "0.1", message = "Dystans musi być większy niż 0")
    private Double distanceKm;
}
