package com.orientapp.exception;

/**
 * Wyjątek rzucany, gdy żądana encja nie istnieje w bazie danych.
 * Obsługiwany przez {@link GlobalExceptionHandler} i mapowany na HTTP 404.
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Tworzy wyjątek z podanym komunikatem.
     *
     * @param message opis błędu (np. „Nie znaleziono zawodów o id: 5”)
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
