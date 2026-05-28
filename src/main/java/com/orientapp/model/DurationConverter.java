package com.orientapp.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

/**
 * Konwerter JPA mapujący {@link Duration} na liczbę sekund ({@link Long}) w bazie danych.
 * <p>
 * Dzięki {@code autoApply = true} jest stosowany automatycznie dla wszystkich
 * pól typu {@link Duration} w encjach (np. czas ukończenia w {@link Result}).
 */
@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    /**
     * Konwertuje czas trwania na liczbę sekund zapisywaną w kolumnie.
     *
     * @param duration czas trwania (może być {@code null})
     * @return liczba sekund lub {@code null} gdy wejście jest {@code null}
     */
    @Override
    public Long convertToDatabaseColumn(Duration duration) {
        return duration == null ? null : duration.getSeconds();
    }

    /**
     * Odtwarza czas trwania z liczby sekund zapisanej w bazie.
     *
     * @param seconds liczba sekund (może być {@code null})
     * @return czas trwania lub {@code null} gdy wejście jest {@code null}
     */
    @Override
    public Duration convertToEntityAttribute(Long seconds) {
        return seconds == null ? null : Duration.ofSeconds(seconds);
    }
}
