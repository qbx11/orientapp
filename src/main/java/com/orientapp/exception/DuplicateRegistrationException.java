package com.orientapp.exception;

/**
 * Wyjątek rzucany przy próbie ponownego zapisu tego samego zawodnika
 * na te same zawody. Obsługiwany przez {@link GlobalExceptionHandler}.
 */
public class DuplicateRegistrationException extends RuntimeException {

    /**
     * Tworzy wyjątek z komunikatem zawierającym dane zawodnika i zawodów.
     *
     * @param username  nazwa/identyfikator zawodnika
     * @param eventName nazwa zawodów
     */
    public DuplicateRegistrationException(String username, String eventName) {
        super("Zawodnik \"" + username + "\" jest już zapisany na zawody \"" + eventName + "\".");
    }
}
