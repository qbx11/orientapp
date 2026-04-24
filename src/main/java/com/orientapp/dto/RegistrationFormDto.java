package com.orientapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationFormDto {

    @NotBlank(message = "Imię i nazwisko jest wymagane")
    @Size(min = 2, max = 100, message = "Imię i nazwisko musi mieć od 2 do 100 znaków")
    private String fullName;

    private String club;

    @NotBlank(message = "Numer chipa jest wymagany")
    @Pattern(regexp = "\\d{6,8}", message = "Numer chipa musi zawierać 6–8 cyfr")
    private String chipNumber;

    @NotNull(message = "Wybierz kategorię")
    private Long categoryId;
}
