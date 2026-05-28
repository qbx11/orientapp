package com.orientapp.exception;

/**
 * Wyjątek rzucany przy próbie rejestracji na zawody, które nie są
 * w statusie {@code OPEN}. Obsługiwany przez {@link GlobalExceptionHandler}.
 */
public class RegistrationClosedException extends RuntimeException {

    /**
     * Tworzy wyjątek z komunikatem zawierającym nazwę zawodów.
     *
     * @param eventName nazwa zawodów
     */
    public RegistrationClosedException(String eventName) {
        super("Rejestracja na zawody \"" + eventName + "\" jest zamknięta.");
    }
}
