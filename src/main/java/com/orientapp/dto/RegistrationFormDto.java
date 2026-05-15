package com.orientapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RegistrationFormDto {

    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 50, message = "Imię musi mieć od 2 do 50 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 50, message = "Nazwisko musi mieć od 2 do 50 znaków")
    private String lastName;

    @NotNull(message = "Data urodzenia jest wymagana")
    @Past(message = "Data urodzenia musi być w przeszłości")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;

    @NotBlank(message = "Adres e-mail jest wymagany")
    @Email(message = "Podaj prawidłowy adres e-mail")
    private String email;

    @Pattern(regexp = "^(\\+?[\\d\\s\\-]{7,15})?$", message = "Podaj prawidłowy numer telefonu")
    private String phone;

    private String club;

    @NotBlank(message = "Numer chipa jest wymagany")
    @Pattern(regexp = "\\d{6,8}", message = "Numer chipa musi zawierać 6–8 cyfr")
    private String chipNumber;

    @NotNull(message = "Wybierz kategorię")
    private Long categoryId;
}
