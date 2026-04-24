package com.orientapp.exception;

public class DuplicateRegistrationException extends RuntimeException {
    public DuplicateRegistrationException(String username, String eventName) {
        super("Zawodnik \"" + username + "\" jest już zapisany na zawody \"" + eventName + "\".");
    }
}
