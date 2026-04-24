package com.orientapp.exception;

public class RegistrationClosedException extends RuntimeException {
    public RegistrationClosedException(String eventName) {
        super("Rejestracja na zawody \"" + eventName + "\" jest zamknięta.");
    }
}
