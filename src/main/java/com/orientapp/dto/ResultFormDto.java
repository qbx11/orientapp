package com.orientapp.dto;

import lombok.Data;

/**
 * DTO formularza wprowadzania wyniku zawodnika w panelu administratora.
 */
@Data
public class ResultFormDto {
    /** Identyfikator zgłoszenia, którego dotyczy wynik. */
    private Long registrationId;
    /** Identyfikator zawodów (do przekierowania po zapisie). */
    private Long eventId;
    /** Czas ukończenia w formacie {@code HH:mm:ss} (z pola input type="time"). */
    private String finishTime;
    /** Flaga „nie ukończył” (Did Not Finish). */
    private boolean dnf;
}
