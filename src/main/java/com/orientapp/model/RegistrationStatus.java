package com.orientapp.model;

public enum RegistrationStatus {
    PENDING("Oczekujące"),
    APPROVED("Zatwierdzone"),
    REJECTED("Odrzucone");

    private final String label;

    RegistrationStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
