package com.orientapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Nie znaleziono");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(RegistrationClosedException.class)
    public String handleRegistrationClosed(RegistrationClosedException ex,
                                           RedirectAttributes ra) {
        log.warn("RegistrationClosedException: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(DuplicateRegistrationException.class)
    public String handleDuplicateRegistration(DuplicateRegistrationException ex,
                                              RedirectAttributes ra) {
        log.warn("DuplicateRegistrationException: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("errorTitle", "Błąd serwera");
        model.addAttribute("errorMessage", "Wystąpił nieoczekiwany błąd. Spróbuj ponownie później.");
        return "error/500";
    }
}
