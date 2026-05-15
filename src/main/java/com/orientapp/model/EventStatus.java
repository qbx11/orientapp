package com.orientapp.model;

public enum EventStatus {
    DRAFT("Wkrótce"),
    OPEN("Zapisy otwarte"),
    CLOSED("Zakończone");

    private final String label;

    EventStatus(String label) { this.label = label; }

    public String getLabel() { return label; }
}
