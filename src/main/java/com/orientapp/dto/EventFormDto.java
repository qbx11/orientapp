package com.orientapp.dto;

import com.orientapp.model.EventStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO formularza tworzenia i edycji zawodów w panelu administratora.
 * Walidacja Bean Validation pilnuje wymaganych pól oraz limitów długości tekstu.
 */
@Data
public class EventFormDto {

    /** Nazwa zawodów (wymagana, maks. 255 znaków). */
    @NotBlank(message = "Nazwa jest wymagana")
    @Size(max = 255, message = "Nazwa może mieć maksymalnie 255 znaków")
    private String name;

    /** Data rozegrania zawodów (wymagana, nie z przeszłości). */
    @NotNull(message = "Data jest wymagana")
    @FutureOrPresent(message = "Data nie może być z przeszłości")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    /** Nazwa miejscowości zawodów (wymagana, maks. 255 znaków). */
    @NotBlank(message = "Lokalizacja jest wymagana")
    @Size(max = 255, message = "Lokalizacja może mieć maksymalnie 255 znaków")
    private String location;

    /** Szerokość geograficzna lokalizacji (uzupełniana po geokodowaniu). */
    private Double latitude;
    /** Długość geograficzna lokalizacji (uzupełniana po geokodowaniu). */
    private Double longitude;

    /** Opis zawodów (opcjonalny, maks. 2000 znaków). */
    @Size(max = 2000, message = "Opis może mieć maksymalnie 2000 znaków")
    private String description;

    /** Treść regulaminu (opcjonalna, maks. 20000 znaków). */
    @Size(max = 20000, message = "Regulamin może mieć maksymalnie 20000 znaków")
    private String regulations;

    /** Status zawodów; domyślnie {@link EventStatus#DRAFT}. */
    @NotNull
    private EventStatus status = EventStatus.DRAFT;
}
