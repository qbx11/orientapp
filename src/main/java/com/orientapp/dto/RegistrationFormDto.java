package com.orientapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO publicznego formularza rejestracji zawodnika na zawody.
 * Walidacja Bean Validation pilnuje poprawności danych osobowych,
 * adresu e-mail, numeru telefonu oraz numeru chipa SI.
 */
@Data
public class RegistrationFormDto {

    /** Imię zawodnika (2–50 znaków). */
    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 50, message = "Imię musi mieć od 2 do 50 znaków")
    private String firstName;

    /** Nazwisko zawodnika (2–50 znaków). */
    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 50, message = "Nazwisko musi mieć od 2 do 50 znaków")
    private String lastName;

    /** Data urodzenia (wymagana, w przeszłości). */
    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    /** Adres e-mail zawodnika (wymagany, poprawny format). */
    @NotBlank(message = "Adres e-mail jest wymagany")
    @Email(message = "Podaj prawidłowy adres e-mail")
    private String email;

    /** Numer telefonu (opcjonalny, 7–15 cyfr/znaków). */
    @Pattern(regexp = "^(\\+?[\\d\\s\\-]{7,15})?$", message = "Podaj prawidłowy numer telefonu")
    private String phone;

    /** Klub sportowy (opcjonalny). */
    private String club;

    /** Numer chipa SI (wymagany, 6–8 cyfr). */
    @NotBlank(message = "Numer chipa jest wymagany")
    @Pattern(regexp = "\\d{6,8}", message = "Numer chipa musi zawierać 6–8 cyfr")
    private String chipNumber;

    /** Identyfikator wybranej kategorii (wymagany). */
    @NotNull(message = "Wybierz kategorię")
    private Long categoryId;
}
