package com.orientapp.model;

/**
 * Status zgłoszenia zawodnika w procesie zatwierdzania przez administratora.
 */
public enum RegistrationStatus {
    /** Oczekuje na decyzję administratora. */
    PENDING("Oczekujące"),
    /** Zatwierdzone — zawodnik dopuszczony do startu. */
    APPROVED("Zatwierdzone"),
    /** Odrzucone przez administratora. */
    REJECTED("Odrzucone");

    private final String label;

    RegistrationStatus(String label) { this.label = label; }

    /**
     * Zwraca etykietę statusu do wyświetlenia w interfejsie.
     *
     * @return czytelna nazwa statusu po polsku
     */
    public String getLabel() { return label; }
}
