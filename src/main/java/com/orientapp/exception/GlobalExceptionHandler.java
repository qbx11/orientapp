package com.orientapp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Globalny handler wyjątków aplikacji ({@link ControllerAdvice}).
 * <p>
 * Mapuje wyjątki domenowe na odpowiednie widoki błędów lub przekierowania
 * z komunikatem flash, zapewniając spójną obsługę błędów w całej aplikacji.
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Obsługuje brak encji — zwraca widok 404.
     *
     * @param ex    przechwycony wyjątek
     * @param model model widoku, do którego dodawany jest komunikat błędu
     * @return nazwa widoku strony błędu 404
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EntityNotFoundException ex, Model model) {
        model.addAttribute("errorTitle", "Nie znaleziono");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Obsługuje próbę rejestracji na zamknięte zawody — przekierowuje na stronę główną
     * z komunikatem flash.
     *
     * @param ex przechwycony wyjątek
     * @param ra atrybuty przekierowania (komunikat flash)
     * @return przekierowanie na stronę główną
     */
    @ExceptionHandler(RegistrationClosedException.class)
    public String handleRegistrationClosed(RegistrationClosedException ex,
                                           RedirectAttributes ra) {
        log.warn("RegistrationClosedException: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Obsługuje duplikat zgłoszenia — przekierowuje na stronę główną z komunikatem flash.
     *
     * @param ex przechwycony wyjątek
     * @param ra atrybuty przekierowania (komunikat flash)
     * @return przekierowanie na stronę główną
     */
    @ExceptionHandler(DuplicateRegistrationException.class)
    public String handleDuplicateRegistration(DuplicateRegistrationException ex,
                                              RedirectAttributes ra) {
        log.warn("DuplicateRegistrationException: {}", ex.getMessage());
        ra.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    /**
     * Obsługuje wszystkie nieprzewidziane wyjątki — zwraca widok 500.
     *
     * @param ex    przechwycony wyjątek
     * @param model model widoku, do którego dodawany jest komunikat błędu
     * @return nazwa widoku strony błędu 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneric(Exception ex, Model model) {
        log.error("Unhandled exception", ex);
        model.addAttribute("errorTitle", "Błąd serwera");
        model.addAttribute("errorMessage", "Wystąpił nieoczekiwany błąd. Spróbuj ponownie później.");
        return "error/500";
    }
}
