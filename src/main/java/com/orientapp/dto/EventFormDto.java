package com.orientapp.dto;

import com.orientapp.model.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EventFormDto {

    @NotBlank(message = "Nazwa jest wymagana")
    @Size(max = 255, message = "Nazwa może mieć maksymalnie 255 znaków")
    private String name;

    @NotNull(message = "Data jest wymagana")
    @FutureOrPresent(message = "Data nie może być z przeszłości")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @NotBlank(message = "Lokalizacja jest wymagana")
    @Size(max = 255, message = "Lokalizacja może mieć maksymalnie 255 znaków")
    private String location;

    private Double latitude;
    private Double longitude;

    @Size(max = 2000, message = "Opis może mieć maksymalnie 2000 znaków")
    private String description;

    @Size(max = 20000, message = "Regulamin może mieć maksymalnie 20000 znaków")
    private String regulations;

    @NotNull
    private EventStatus status = EventStatus.DRAFT;
}
