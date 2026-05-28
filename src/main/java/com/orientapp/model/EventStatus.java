package com.orientapp.model;

/**
 * Status zawodów określający etap ich cyklu życia.
 * Rejestracja zawodników możliwa jest wyłącznie w statusie {@link #OPEN}.
 */
public enum EventStatus {
    /** Wersja robocza — zawody jeszcze nieopublikowane. */
    DRAFT("Wkrótce"),
    /** Zapisy otwarte — można rejestrować zawodników. */
    OPEN("Zapisy otwarte"),
    /** Zawody zakończone — zapisy zamknięte. */
    CLOSED("Zakończone");

    private final String label;

    EventStatus(String label) { this.label = label; }

    /**
     * Zwraca etykietę statusu do wyświetlenia w interfejsie.
     *
     * @return czytelna nazwa statusu po polsku
     */
    public String getLabel() { return label; }
}
